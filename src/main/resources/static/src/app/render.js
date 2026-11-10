/* PetCARE — Main render loop (auth + role guard applied on every render) */
function render(){
  const root = document.getElementById("app");
  if(!root) return;
  const user = AuthService.getCurrentUser();
  if(user && !AuthGuard.canAccess(user.role, UI.screen)) UI.screen = AuthGuard.homeScreen(user.role);
  UI.view = user ? "app" : "login";
  let body = user ? renderApp() : renderAuth();
  if(!user && UI.authScreen !== "landing") body += themeToggleButton("floating");
  if(UI.modal){ body += `<div class="modal-overlay" id="modal-overlay">${UI.modal}</div>`; }
  body += `<div class="toast-wrap" id="toast-wrap">${UI.toasts.map(t=>`<div class="toast">${esc(t.msg)}</div>`).join("")}</div>`;
  root.innerHTML = body;
  syncHash();
  attachEvents(root);
  bindModernExperience(root);
}
