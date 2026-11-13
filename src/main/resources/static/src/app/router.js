/* DogCARE — Hash router with role guard
   URL shape: #/<portal|staff|admin>/<screen>. A URL that does not belong to the
   signed-in role is ignored and the user lands on their own dashboard. */
function applyHashRoute(){
  const user = AuthService.getCurrentUser();
  if(!user) return;
  const r = AuthGuard.parseHash(location.hash);
  if(!r) return;
  if(r.path !== AuthGuard.rolePath(user.role) || !AuthGuard.isTopLevel(user.role, r.screen)){
    UI.screen = AuthGuard.homeScreen(user.role);
    return;
  }
  if(r.screen !== UI.screen && ["petregistration","requestservice","adoptapply","walkin"].includes(r.screen)) resetWizardsForScreen(r.screen);
  UI.screen = r.screen;
}
function syncHash(){
  const user = AuthService.getCurrentUser();
  const publicRoute = UI.authScreen === "landing" ? "home" : UI.authScreen;
  const h = user ? `#/${AuthGuard.rolePath(user.role)}/${UI.screen}` : `#/${publicRoute}`;
  if(location.hash !== h) history.replaceState(null, "", h);
  document.title = "DogCARE — City Veterinary Office, Santa Rosa, Laguna";
}
