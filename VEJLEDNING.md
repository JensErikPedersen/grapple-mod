# Gribekrog-mod: byg og test på din PC

Mod'en tilføjer en **gribekrog** til Minecraft 26.2 med Fabric.

## Sådan virker den

- **Højreklik** med gribekrogen i hånden for at skyde krogen afsted. Du hører samme lyd som når man skyder med en bue.
- Krogen flyver lige ud, hvor du sigter. Rebet er **35 blokke** langt.
- **Rammer krogen en blok** (et træ, en klippe, en mur), sætter den sig fast, og du bliver trukket op mod den.
- **Når du når krogen**, slipper du rebet og flyver videre med den fart, du har, plus et lille hop opad, så du kan komme over en kant. Så daler du ned igen. Pas på, for et langt fald giver faldskade ligesom ellers.
- **Rammer krogen ikke noget**, kommer den og rebet tilbage til dig.
- **Højreklik igen** eller tryk **Shift** for at slippe rebet før tid. Du beholder farten.
- Rebet og krogen vises som en række små brune og grå prikker.

**Opskrift** (crafting table):

| | | |
|---|---|---|
| | Jernbarre | Snubletrådskrog |
| | Snor | Jernbarre |
| Snor | | |

Tingene skal ligge præcis sådan i gitteret (eller spejlvendt). Opskriften dukker op i opskriftsbogen, så snart du har samlet snor, en jernbarre eller en snubletrådskrog op.

I kreativ tilstand ligger gribekrogen under *Værktøj og hjælpemidler*.

## 1. Værktøjer

Du har allerede Java 25, IntelliJ, Fabric Loader og Fabric API fra rubin-mod'en. Der skal ikke installeres noget nyt.

## 2. Åbn projektet

1. Pak `grapple-mod.zip` ud til fx `C:\Users\jense\Documents\grapple-mod`.
2. I IntelliJ: **File → Open** og vælg mappen `grapple-mod`. Vælg "New Window", så rubin-mod'en stadig er åben i det andet vindue.
3. Vent til IntelliJ er færdig med at hente Minecraft og Fabric (bjælken nederst).
4. Hvis IntelliJ klager over Java: *File → Settings → Build, Execution, Deployment → Build Tools → Gradle*, sæt **Gradle JVM** til Java 25.

## 3. Test fra IntelliJ

1. Åbn Gradle-panelet i højre side.
2. Dobbeltklik på **Tasks → fabric → runClient**.
3. Lav en ny verden i kreativ tilstand, find gribekrogen under *Værktøj og hjælpemidler*, og skyd mod et træ.

## 4. Byg en .jar til det rigtige spil

1. I terminalen nederst i IntelliJ: `.\gradlew build`
2. Filen ligger i `grapple-mod\build\libs\grapplemod-1.0.1.jar`.
3. Kopiér den til `C:\Users\jense\AppData\Roaming\.minecraft\mods` (ved siden af rubin-mod'en og Fabric API).
4. Start profilen **fabric-loader-26.2** i Minecraft Launcher.

## Hvis noget går galt

- **Røde fejl når du bygger** (fx "cannot find symbol"): Kopiér den røde tekst og send den til mig. Mod'en er skrevet uden at kunne bygges i skyen, så et navn i Minecrafts kode kan have ændret sig.
- **Spillet crasher:** Send mig filen `run\logs\latest.log` (når du tester fra IntelliJ) eller `.minecraft\logs\latest.log`.

## Hvor er hvad i koden

| Fil | Hvad den gør |
|---|---|
| `src/main/java/dk/jens/grapplemod/GrappleMod.java` | Startpunktet. Starter gribekrogen og får serveren til at opdatere krogene 20 gange i sekundet. |
| `src/main/java/dk/jens/grapplemod/GrapplingHookItem.java` | Selve item'et. Bestemmer hvad der sker ved højreklik. |
| `src/main/java/dk/jens/grapplemod/GrappleManager.java` | Hjernen: krogens flyvning, rebet, trækket og lydene. **Øverst i filen kan du ændre rebets længde, farten og meget mere.** |
| `src/main/java/dk/jens/grapplemod/ModItems.java` | Registrerer gribekrogen og lægger den i kreativ-menuen. |
| `assets/grapplemod/textures/item/grappling_hook.png` | Billedet af gribekrogen (16×16 pixels). |
| `assets/grapplemod/lang/` | Navnet på dansk og engelsk. |
| `data/grapplemod/recipe/grappling_hook.json` | Opskriften. |
| `gradle.properties` | Versionsnumre for Minecraft, Fabric og din mod. |
