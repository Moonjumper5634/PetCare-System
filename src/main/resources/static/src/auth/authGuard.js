/* PetCARE — Role-based access control (centralized)
   Every screen a role may open is derived from its navigation menu plus the
   detail screens reachable from that menu. render() and the URL router both
   call canAccess(), so a Resident can never reach Staff/Admin pages.
   FUTURE BACKEND: the API must enforce the same rules server-side; this guard
   only controls what the UI shows. */
const AuthGuard = (function(){
  const DETAIL_SCREENS = {
    owner:["petprofile","adoptdetail","adoptapply","adoptsubmitted"],
    staff:["regdetail","reqdetail","adoptreview"],
    admin:[],
  };
  const ROLE_PATHS = { owner:"portal", staff:"staff", admin:"admin" };

  function topLevelScreens(role){ return (navs[role] || []).filter(i => i.id).map(i => i.id); }
  function canAccess(role, screen){
    return topLevelScreens(role).includes(screen) || (DETAIL_SCREENS[role] || []).includes(screen);
  }
  function isTopLevel(role, screen){ return topLevelScreens(role).includes(screen); }
  function homeScreen(){ return "dashboard"; }
  function rolePath(role){ return ROLE_PATHS[role] || "login"; }
  function parseHash(hash){
    const m = /^#\/([a-z]+)\/([a-z]+)$/.exec(hash || "");
    return m ? { path:m[1], screen:m[2] } : null;
  }
  return { canAccess, isTopLevel, homeScreen, rolePath, parseHash };
})();
