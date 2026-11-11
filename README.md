# Grapple Mod

A Fabric mod for Minecraft 26.2 that adds a **Grappling Hook** you can fire at trees, cliffs and walls to pull yourself up.

## How it works

- **Right-click** to fire the hook straight where you aim. The rope is **35 blocks** long.
- **If the hook hits a block**, it sticks and you are pulled towards it.
- **When you reach the hook**, the rope releases and you keep your momentum, plus a small upward boost to get over ledges. Fall damage still applies.
- **If the hook misses**, it flies back to you.
- **Right-click again** or press **Shift** to let go early. You keep your speed.
- The rope and hook are drawn as small brown and grey particles.

## Recipe

Crafting table, shaped (can be mirrored):

| | | |
|---|---|---|
| | Iron Ingot | Tripwire Hook |
| | String | Iron Ingot |
| String | | |

The recipe shows up in the recipe book once you pick up string, an iron ingot or a tripwire hook. In Creative mode, the hook is in the *Tools & Utilities* tab.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.5 or newer
- Fabric API
- Java 25

## Build and run

Test in a development client:

```
.\gradlew runClient
```

Build the mod jar:

```
.\gradlew build
```

Then copy `build/libs/grapplemod-1.0.1.jar` into your `.minecraft/mods` folder and start the Fabric 26.2 profile.

## Tuning

All values are at the top of `src/main/java/dk/jens/grapplemod/GrappleManager.java`:

| Value | Default | What it does |
|---|---|---|
| `ROPE_LENGTH` | 35 | Rope length in blocks |
| `HOOK_SPEED` | 2.5 | How fast the hook flies out (blocks per tick) |
| `RETURN_SPEED` | 3.5 | How fast a missed hook returns |
| `PULL_SPEED` | 1.6 | Top speed while being pulled |
| `PULL_ACCELERATION` | 0.25 | How quickly you reach top speed (0–1) |
| `RELEASE_LIFT` | 0.5 | Upward boost when the rope releases |
| `MAX_PULL_TICKS` | 100 | Releases after 5 seconds no matter what |

## Where things are

| Path | Contents |
|---|---|
| `src/main/java/dk/jens/grapplemod/GrappleManager.java` | Hook flight, pulling, release and rope particles |
| `src/main/java/dk/jens/grapplemod/GrapplingHookItem.java` | The item and its right-click |
| `src/main/resources/data/grapplemod/recipe/` | Crafting recipe |
| `src/main/resources/assets/grapplemod/textures/item/` | Item texture |
| `src/main/resources/assets/grapplemod/lang/` | English and Danish names |
