/* PetCARE — Sidebar icon set */

const ICON_PATHS = {
  dashboard:'<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>',
  mypets:'<circle cx="12" cy="9" r="4"/><path d="M5 21c0-3.5 3-6 7-6s7 2.5 7 6"/>',
  petregistration:'<path d="M6 3h9l4 4v14a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z"/><path d="M9 12l2 2 4-4"/>',
  requestservice:'<path d="M12 3v6l4 2"/><circle cx="12" cy="12" r="9"/>',
  myrequests:'<rect x="3.5" y="4" width="17" height="16" rx="2"/><path d="M3.5 9h17"/><path d="M8 13h8"/><path d="M8 16h5"/>',
  adopt:'<path d="M12 20s-7-4.4-9.5-9A5 5 0 0 1 12 6a5 5 0 0 1 9.5 5c-2.5 4.6-9.5 9-9.5 9Z"/>',
  notifications:'<path d="M6 9a6 6 0 0 1 12 0c0 5 2 6 2 6H4s2-1 2-6Z"/><path d="M10 20a2 2 0 0 0 4 0"/>',
  profile:'<circle cx="12" cy="8" r="3.5"/><path d="M5 20c0-3.5 3-6.5 7-6.5s7 3 7 6.5"/>',
  petregistrations:'<path d="M6 3h9l4 4v14a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z"/><path d="M9 12l2 2 4-4"/>',
  servicerequests:'<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3.5 2"/>',
  eligibility:'<path d="M12 3l7 3v6c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V6l7-3Z"/><path d="M9 12l2 2 4-4.5"/>',
  adoptionapplications:'<path d="M12 20s-7-4.4-9.5-9A5 5 0 0 1 12 6a5 5 0 0 1 9.5 5c-2.5 4.6-9.5 9-9.5 9Z"/>',
  servicesrecords:'<path d="M4 4h16v14H8l-4 4V4Z"/><path d="M8 9h8"/><path d="M8 13h5"/>',
  walkin:'<circle cx="12" cy="8" r="3.5"/><path d="M5 20c0-3.5 3-6.5 7-6.5s7 3 7 6.5"/>',
  reports:'<path d="M4 20V10"/><path d="M11 20V4"/><path d="M18 20v-7"/>',
  auditlogs:'<rect x="4" y="3" width="16" height="18" rx="2"/><path d="M8 8h8"/><path d="M8 12h8"/><path d="M8 16h5"/>',
  usermanagement:'<circle cx="9" cy="8" r="3.2"/><path d="M3 20c0-3.3 2.7-6 6-6s6 2.7 6 6"/><path d="M16 8.5a3 3 0 1 1 3.2 3"/><path d="M21 20c-.2-2.4-1.6-4.3-3.6-5.2"/>',
  petrecords:'<rect x="3.5" y="4" width="17" height="16" rx="2"/><path d="M3.5 9h17"/><path d="M8 4v5"/>',
  servicemanagement:'<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3.5 2"/>',
  eligibilitycriteria:'<path d="M4 6h16"/><path d="M4 12h10"/><path d="M4 18h7"/><circle cx="19" cy="12" r="2"/><circle cx="16" cy="18" r="2"/>',
  systemsettings:'<circle cx="12" cy="12" r="3"/><path d="M19.4 13a7.6 7.6 0 0 0 0-2l2-1.5-2-3.5-2.4 1a7.8 7.8 0 0 0-1.7-1L15 3h-6l-.3 2.5a7.8 7.8 0 0 0-1.7 1l-2.4-1-2 3.5L4.6 11a7.6 7.6 0 0 0 0 2l-2 1.5 2 3.5 2.4-1a7.8 7.8 0 0 0 1.7 1L9 21h6l.3-2.5a7.8 7.8 0 0 0 1.7-1l2.4 1 2-3.5-2-1.5Z"/>',
  faq:'<circle cx="12" cy="12" r="9"/><path d="M9.5 9a2.5 2.5 0 0 1 5 0c0 1.7-2 2-2 3.5"/><path d="M12 17h.01"/>',
};
function navIcon(id){
  const path = ICON_PATHS[id] || ICON_PATHS.dashboard;
  return `<svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">${path}</svg>`;
}
