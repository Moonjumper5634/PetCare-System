/* PetCARE — PetCARE seal and inline SVG icons */

/* ---------------- PETCARE LOGO (shield + paw print) ----------------
   Used consistently in the login screen and the app sidebar/topbar. */
function sealSvg(size){
  size = size || 44;
  return `<div class="seal-placeholder" style="width:${size}px;height:${size}px;">
    <svg width="${size}" height="${size}" viewBox="0 0 24 24">
      <path d="M12 2.2 4.2 5.1v5.5c0 5.5 3.3 9.8 7.8 11 4.5-1.2 7.8-5.5 7.8-11V5.1L12 2.2Z" fill="#ffffff" stroke="#B4222F" stroke-width="1.3" stroke-linejoin="round"/>
      <g fill="#C22030">
        <ellipse cx="12" cy="14.6" rx="3.1" ry="2.5"/>
        <circle cx="8.4" cy="10.5" r="1.15"/>
        <circle cx="10.7" cy="8.3" r="1.15"/>
        <circle cx="13.3" cy="8.3" r="1.15"/>
        <circle cx="15.6" cy="10.5" r="1.15"/>
      </g>
    </svg>
  </div>`;
}

/* ---------------- SMALL LINE-ICON HELPER (replaces decorative emoji) ---------------- */
const ICO_SVGS = {
  doc:'<path d="M6 3h9l4 4v14a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z"/><path d="M9 12h6"/><path d="M9 16h6"/>',
  check:'<circle cx="12" cy="12" r="9"/><path d="M8 12.3l2.5 2.5L16 9.5"/>',
  ban:'<circle cx="12" cy="12" r="9"/><path d="M5.5 5.5l13 13"/>',
  bell:'<path d="M6 9a6 6 0 0 1 12 0c0 5 2 6 2 6H4s2-1 2-6Z"/><path d="M10 20a2 2 0 0 0 4 0"/>',
  folder:'<path d="M3 6.5a1 1 0 0 1 1-1h5l2 2h9a1 1 0 0 1 1 1V18a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V6.5Z"/>',
  paw:'<circle cx="12" cy="15" r="4"/><circle cx="6" cy="8" r="2"/><circle cx="18" cy="8" r="2"/><circle cx="9" cy="5.5" r="1.8"/><circle cx="15" cy="5.5" r="1.8"/>',
  vet:'<path d="M12 3l7 3v6c0 4.7-3.1 8-7 9-3.9-1-7-4.3-7-9V6l7-3Z"/><path d="M12 8v6"/><path d="M9 11h6"/>',
  chart:'<path d="M4 20V10"/><path d="M11 20V4"/><path d="M18 20v-7"/>',
};
function icoSvg(name, size){
  size = size || 40;
  const path = ICO_SVGS[name] || ICO_SVGS.doc;
  return `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">${path}</svg>`;
}
