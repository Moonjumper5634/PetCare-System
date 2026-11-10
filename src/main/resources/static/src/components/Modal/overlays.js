/* PetCARE — Toasts, modal and discard-confirmation helpers */

/* ---------------- TOASTS ---------------- */
function showToast(msg){
  UI.toasts.push({id:Date.now()+Math.random(), msg});
  render();
  setTimeout(()=>{ UI.toasts.shift(); renderToastsOnly(); }, 3200);
}
function renderToastsOnly(){
  const wrap = document.getElementById("toast-wrap");
  if(wrap) wrap.innerHTML = UI.toasts.map(t=>`<div class="toast">${esc(t.msg)}</div>`).join("");
}

/* ---------------- MODAL ---------------- */
function openModal(html){ UI.modal = html; render(); }
function closeModal(){ UI.modal = null; render(); }

function confirmCancel(discardFn){
  openModal(`
    <div class="modal-box">
      <h3>Discard changes?</h3>
      <p style="font-size:0.85rem;color:var(--body);">Are you sure you want to cancel? Your entered information will be lost.</p>
      <div class="modal-actions">
        <button class="btn-line" id="m-continue-edit">Continue Editing</button>
        <button class="btn-red" id="m-discard">Discard</button>
      </div>
    </div>`);
  UI._discardFn = discardFn;
}
