# Phoenix's Chromatic Codes
### *A high-performance Forge (1.20.1) Rendering Framework*

**Phoenix's Chromatic Codes** is a specialized rendering framework designed to bypass the hardcoded limitations of the Minecraft engine. By utilizing `BakedGlyph` Mixins, it introduces a robust, per-character formatting system that supports dynamic gradients, animations, and true hex-color depth.

---

## Advanced Formatting Engine
Unlike standard Minecraft formatting, Phoenix's Chromatic Codes allows for deep customization via the configuration. You can define custom character triggers for both static colors and complex animated effects.

### Where to use Chromatic Codes?
You can use these codes **anywhere standard Minecraft formatting (`&` or `§`) is supported.** This includes:
* **Chat:** Direct messages and global chat.
* **Item Lore:** Machine names, tooltips, and item descriptions.
* **GUIs:** GregTech machine interfaces and custom menus.
* **World Labels:** Signs, books, and server MOTDs.

### Custom Static Colors
Define a character to act as a hex-code shortcut.
* **Format:** `char:hex`
* **Example:** `z:BF00FF` (Triggers purple via `§z`)

### Custom Animated Gradients
The core of the engine supports movement-based formatting that calculates colors at the glyph level.
* **Format:** `char:colorSpeed:moveSpeed:movementId:hex1,hex2...`

| ID | Effect Description |
| :--- | :--- |
| **`wave`** | Smoothly oscillating color shifts across the string. |
| **`shake`** | Jittery, high-energy character offset/color flickering. |
| **`pulse`** | Global brightness/hue cycling. |
| **`static_rainbow`** | A fixed rainbow spread that doesn't move. |
| **`glitch`** | Erratic, digital-style color snapping. |
| **`discord`** | A two-tone smooth scrolling gradient (Nitro-style). |
| **`breath`** | Soft fading in and out of a specific hue. |
| **`stretch`** | Horizontal scaling/distortion of the text color. |

---

##  Example Configurations
Add these to your config file to register new codes:

```yaml
customColors:
  - z:BF00FF

customGradients:
  - "w:1.0:1.0:wave:rainbow"          # Standard moving rainbow
  - "s:0.0:2.5:shake:FFFFFF"         # Pure white vibrating text
  - "^:0.0:0.0:discord:BF00FF,FFB7C5" # Purple-to-Pink scroll
  - "g:1.0:3.0:glitch:00FF00,005500" # Matrix-style digital decay
  - "%:0.0:2.0:stretch:FF00FF"       # Magenta horizontal stretching

##  Extending the API: Adding New Effects
Phoenix's Chromatic Codes uses a **Movement Registry**, allowing you to register custom animation logic without modifying core Mixins.

### 1. Create your Effect Class
Implement `IChromaticEffect` to define your math:

```java
public class MyCustomEffect implements IChromaticEffect {
    private final List<Integer> colors;
    private final float speed;

    public MyCustomEffect(float colorSpeed, float moveSpeed, List<Integer> colors) {
        this.colors = colors;
        this.speed = colorSpeed;
    }

    @Override
    public int getColor(float x, float y, float time) {
        // Return calculated hex color based on position (x, y) and world time
        return colors.get(0); 
    }
}

### 2. Register the Movement ID
Register the factory in your mod constructor so it is available to the configuration parser:

```java
MovementRegistry.register("my_effect_id", (colorSpeed, moveSpeed, colors) -> 
    new MyCustomEffect(colorSpeed, moveSpeed, colors)
);
### Thread Safety
The engine uses `ThreadLocal<IChromaticEffect>` to bridge the gap between the high-level Font system and the low-level GL draw calls safely. This prevents race conditions and data "bleeding" between different text components during frame rendering.

```java
private static final ThreadLocal<IChromaticEffect> CURRENT_EFFECT = new ThreadLocal<>();

public static void setCurrentEffect(IChromaticEffect effect) {
    CURRENT_EFFECT.set(effect);
}

public static IChromaticEffect getCurrentEffect() {
    return CURRENT_EFFECT.get();
}