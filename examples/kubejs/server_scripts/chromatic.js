// kubejs/server_scripts/chromatic.js  -  edit, then /reload. Players get the new codes immediately.
ChromaticEvents.register(event => {
  // Animated effect. Same fields as the config's gradient lists:
  // { movement, colors, colorSpeed (default 1), moveSpeed (default depends on movement) }
  event.effect('q', { movement: 'wave', colors: ['#ff5500', '#ffcc00'], colorSpeed: 1.2, moveSpeed: 1.0 })   // &q
  event.effect('[ember]', { movement: 'glitch', colors: ['ff3300', '440000'] })                                 // &[ember]
  event.effect('[huge]', { movement: 'size', colors: ['#ffffff'], moveSpeed: 2.5 })                             // &[huge]
  event.effect('[prism]', { movement: 'rainbow_wave', colors: 'rainbow', colorSpeed: 1.2 })

  // Static gradient shorthand
  event.gradient('[sunrise]', ['#ff9a3c', '#ff3c7a'])

  // Plain colors
  event.color('[gt_blue]', '#2a6bff')
  event.color('j', 0x00ccaa)

  // Outline codes: &[glow] uses the outline color, &[glow:FF0000] a specific one
  event.outline('[glow]')
  event.outlineColor('#000000')
})

// Helpers available in every script type:
//   Chromatic.movements()                       -> all movement ids
//   Chromatic.inline('wave:FF0000,00FF00', 'Hi') -> a Component with a one-off effect (no registration needed)
//   Chromatic.text('§wHello')                    -> parse §-codes (config/named/inline codes) into a Component
