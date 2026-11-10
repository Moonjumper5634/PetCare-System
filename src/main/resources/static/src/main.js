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
})();
