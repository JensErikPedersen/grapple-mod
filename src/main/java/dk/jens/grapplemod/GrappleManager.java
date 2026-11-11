package dk.jens.grapplemod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Styrer alle gribekroge i verden.
 *
 * En krog går igennem tre faser:
 *   FLYING     krogen flyver lige ud, hvor spilleren sigtede
 *   ATTACHED   krogen har ramt en blok, og spilleren bliver trukket derhen
 *   RETURNING  krogen ramte ikke noget inden for rebets længde og flyver tilbage
 *
 * Alle afstande er i blokke, og alle hastigheder er i blokke pr. tick (20 ticks = 1 sekund).
 */
public class GrappleManager {
	// Prøv at ændre tallene her og se, hvad der sker!
	public static final double ROPE_LENGTH = 35.0;  // rebets længde
	private static final double HOOK_SPEED = 2.5;    // hvor hurtigt krogen flyver ud
	private static final double RETURN_SPEED = 3.5;  // hvor hurtigt krogen kommer tilbage
	private static final double PULL_SPEED = 1.6;    // topfart når spilleren trækkes (ca. som elytra med raket)
	private static final double PULL_ACCELERATION = 0.25; // hvor hurtigt spilleren kommer op i fart (0-1)
	private static final double RELEASE_LIFT = 0.5;  // ekstra hop opad, når man slipper, så man kan komme over en kant
	private static final double ARRIVE_DISTANCE = 1.5; // så tæt på krogen tæller som "fremme"
	private static final int MAX_PULL_TICKS = 100;   // slip efter 5 sekunder, uanset hvad
	private static final int STUCK_TICKS = 10;       // slip hvis man har siddet fast i et halvt sekund
	private static final int CLICK_DELAY_TICKS = 8;  // ignorer nye klik lige efter et skud

	// Rebet og krogen tegnes som små farvede prikker (partikler)
	private static final DustParticleOptions ROPE_PARTICLE = new DustParticleOptions(0x8B5A2B, 0.8f);
	private static final DustParticleOptions HOOK_PARTICLE = new DustParticleOptions(0x6E6E78, 1.6f);
	private static final double ROPE_PARTICLE_SPACING = 0.3;

	private enum Phase { FLYING, ATTACHED, RETURNING }

	// Alt hvad vi skal huske om én spillers krog
	private static class Grapple {
		final ServerLevel level;
		final Vec3 direction;
		final long firedAt;
		Phase phase = Phase.FLYING;
		Vec3 hookPos;
		double travelled = 0;
		BlockPos hitBlock;
		Vec3 velocity = Vec3.ZERO;
		int pullTicks = 0;
		double bestDistance = Double.MAX_VALUE;
		int ticksWithoutProgress = 0;

		Grapple(ServerLevel level, Vec3 start, Vec3 direction, long firedAt) {
			this.level = level;
			this.hookPos = start;
			this.direction = direction;
			this.firedAt = firedAt;
		}
	}

	// Hver spiller kan have én krog ude ad gangen
	private static final Map<UUID, Grapple> GRAPPLES = new HashMap<>();

	// Højreklik: skyd krogen, eller slip rebet hvis krogen allerede er ude
	public static void use(ServerPlayer player, ServerLevel level) {
		Grapple existing = GRAPPLES.get(player.getUUID());
		if (existing != null) {
			if (level.getGameTime() - existing.firedAt < CLICK_DELAY_TICKS) {
				return;
			}
			if (existing.phase == Phase.ATTACHED) {
				release(player, existing);
				GRAPPLES.remove(player.getUUID());
			} else {
				existing.phase = Phase.RETURNING;
			}
			return;
		}

		Grapple grapple = new Grapple(level, player.getEyePosition(), player.getLookAngle(), level.getGameTime());
		GRAPPLES.put(player.getUUID(), grapple);

		// Samme lyd som når man skyder med bue (og samme lidt tilfældige tonehøjde)
		float pitch = 1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + 0.5f;
		playSound(level, player.getEyePosition(), SoundEvents.ARROW_SHOOT, pitch);
	}

	// Kaldes 20 gange i sekundet
	public static void tick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, Grapple>> iterator = GRAPPLES.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, Grapple> entry = iterator.next();
			Grapple grapple = entry.getValue();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

			// Spilleren er logget ud, død eller gået gennem en portal
			if (player == null || !player.isAlive() || player.level() != grapple.level) {
				iterator.remove();
				continue;
			}

			boolean finished = switch (grapple.phase) {
				case FLYING -> tickFlying(player, grapple);
				case ATTACHED -> tickAttached(player, grapple);
				case RETURNING -> tickReturning(player, grapple);
			};

			if (finished) {
				iterator.remove();
			} else {
				drawRope(player, grapple);
			}
		}
	}

	public static void clear() {
		GRAPPLES.clear();
	}

	// Krogen flyver ét stykke frem og tjekker, om den rammer en blok på vejen
	private static boolean tickFlying(ServerPlayer player, Grapple grapple) {
		double step = Math.min(HOOK_SPEED, ROPE_LENGTH - grapple.travelled);
		Vec3 from = grapple.hookPos;
		Vec3 to = from.add(grapple.direction.scale(step));

		BlockHitResult hit = grapple.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		if (hit.getType() == HitResult.Type.BLOCK) {
			grapple.hookPos = hit.getLocation();
			grapple.hitBlock = hit.getBlockPos();
			grapple.phase = Phase.ATTACHED;
			playSound(grapple.level, grapple.hookPos, SoundEvents.ARROW_HIT, 1.2f);
			return false;
		}

		grapple.hookPos = to;
		grapple.travelled += step;
		if (grapple.travelled >= ROPE_LENGTH - 0.001) {
			// Rebet er brugt op uden at ramme noget: krogen kommer tilbage
			grapple.phase = Phase.RETURNING;
		}
		return false;
	}

	// Krogen sidder fast: træk spilleren hen mod den
	private static boolean tickAttached(ServerPlayer player, Grapple grapple) {
		// Shift slipper rebet. Hvis blokken bliver ødelagt, falder krogen af.
		if (player.isShiftKeyDown() || grapple.level.getBlockState(grapple.hitBlock).isAir()) {
			release(player, grapple);
			return true;
		}

		Vec3 body = player.position().add(0, player.getBbHeight() * 0.5, 0);
		Vec3 toHook = grapple.hookPos.subtract(body);
		double distance = toHook.length();

		// Fremme ved krogen: slip og flyv videre med den fart, man har
		if (distance < Math.max(ARRIVE_DISTANCE, grapple.velocity.length() * 1.2)) {
			release(player, grapple);
			return true;
		}

		// Sidder man fast (fx under et udhæng), eller har det taget for lang tid, så slip
		if (distance < grapple.bestDistance - 0.1) {
			grapple.bestDistance = distance;
			grapple.ticksWithoutProgress = 0;
		} else if (++grapple.ticksWithoutProgress > STUCK_TICKS) {
			release(player, grapple);
			return true;
		}
		if (++grapple.pullTicks > MAX_PULL_TICKS) {
			release(player, grapple);
			return true;
		}

		// Ret farten lidt mere ind mod krogen for hvert tick, så man accelererer blødt op
		Vec3 wanted = toHook.scale(PULL_SPEED / distance);
		grapple.velocity = grapple.velocity.add(wanted.subtract(grapple.velocity).scale(PULL_ACCELERATION));
		setPlayerVelocity(player, grapple.velocity);
		return false;
	}

	// Krogen flyver tilbage til spilleren
	private static boolean tickReturning(ServerPlayer player, Grapple grapple) {
		Vec3 toPlayer = ropeStart(player).subtract(grapple.hookPos);
		double distance = toPlayer.length();
		if (distance <= RETURN_SPEED) {
			playSound(grapple.level, player.position(), SoundEvents.FISHING_BOBBER_RETRIEVE, 1.0f);
			return true;
		}
		grapple.hookPos = grapple.hookPos.add(toPlayer.scale(RETURN_SPEED / distance));
		return false;
	}

	// Slip rebet. Spilleren beholder sin fart og får et lille hop opad.
	private static void release(ServerPlayer player, Grapple grapple) {
		setPlayerVelocity(player, grapple.velocity.add(0, RELEASE_LIFT, 0));
	}

	private static void setPlayerVelocity(ServerPlayer player, Vec3 velocity) {
		player.setDeltaMovement(velocity);
		// Fortæl spillerens klient om den nye fart (det samme som når man bliver slået tilbage)
		player.hurtMarked = true;
		// Turen op mod krogen skal ikke tælle med som et fald
		player.resetFallDistance();
	}

	private static Vec3 ropeStart(ServerPlayer player) {
		return player.position().add(0, player.getBbHeight() * 0.6, 0).add(player.getLookAngle().scale(0.4));
	}

	private static void drawRope(ServerPlayer player, Grapple grapple) {
		Vec3 start = ropeStart(player);
		Vec3 line = grapple.hookPos.subtract(start);
		int points = (int) (line.length() / ROPE_PARTICLE_SPACING);
		for (int i = 1; i < points; i++) {
			Vec3 point = start.add(line.scale(i / (double) points));
			grapple.level.sendParticles(ROPE_PARTICLE, point.x, point.y, point.z, 1, 0, 0, 0, 0);
		}
		Vec3 hook = grapple.hookPos;
		grapple.level.sendParticles(HOOK_PARTICLE, hook.x, hook.y, hook.z, 3, 0.05, 0.05, 0.05, 0);
	}

	private static void playSound(ServerLevel level, Vec3 position, SoundEvent sound, float pitch) {
		level.playSound(null, position.x, position.y, position.z, sound, SoundSource.PLAYERS, 1.0f, pitch);
	}
}
