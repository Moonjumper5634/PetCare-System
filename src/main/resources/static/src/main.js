/* PetCARE — Application entry point */
(async function boot(){
  await AuthService.restore();      // keeps you signed in across page reloads while the server session is valid
  if(!AuthService.getCurrentUser()){
    const publicRoute = /^#\/(home|login|signup)$/.exec(location.hash || "");
    if(publicRoute) UI.authScreen = publicRoute[1] === "home" ? "landing" : publicRoute[1];
  }
  applyHashRoute();
  render();
  window.addEventListener("hashchange", () => { applyHashRoute(); render(); });
  // Keep dashboard totals, queues and notification badges current when another user changes data.
  setInterval(async()=>{
    if(document.hidden || !AuthService.getCurrentUser() || UI._busy || UI.modal) return;
    const currentScreen=UI.screen;
    const refreshed=await hydrate();
    if(refreshed.ok && (currentScreen==="dashboard" || currentScreen==="notifications")) render();
  },15000);
})();
