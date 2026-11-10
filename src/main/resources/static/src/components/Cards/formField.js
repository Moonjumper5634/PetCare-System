/* PetCARE — Labelled form field helper */

/* ---------------- LOGIN / SIGNUP / VERIFY / FORGOT SCREENS ---------------- */
function field(label, inner, errKey){
  const err = UI.errors && UI.errors[errKey];
  return `<div class="field ${err?"error":""}"><label>${label}</label>${inner}${err?`<div class="field-error-msg">${esc(err)}</div>`:""}</div>`;
}
