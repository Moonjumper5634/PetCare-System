/* DogCARE — Page: eligibilityCriteria */

/* ---------------- ADMIN: ELIGIBILITY CRITERIA ---------------- */
function screenEligibilityCriteria(){
  const svcBlocks = Object.keys(S.criteria).map(svc=>{
    const items = S.criteria[svc];
    return `
    <div class="card">
      <div style="display:flex;justify-content:space-between;gap:12px;align-items:center;"><h3>${esc(svc)}</h3><button class="btn-sm" data-addcriterion="${esc(svc)}">+ Add Requirement</button></div>
      ${items.map((c,idx)=>`<div class="checklist-row"><span>${esc(c.label)} — <span style="color:var(--muted);">${esc(c.desc)}</span></span>
        <span>
          ${c.enabled?pill("green:Active"):pill("gray:Disabled")}
          <button class="btn-sm" data-editcriterion="${svc}|${idx}">Edit</button>
          <button class="btn-sm ${c.enabled?"danger":""}" data-togglecriterion="${svc}|${idx}">${c.enabled?"Disable":"Enable"}</button>
        </span>
      </div>`).join("")}
    </div>`;
  }).join("");
  return `
  <div class="page-head"><div><div class="crumb">Administration</div><h2>Eligibility Criteria Configuration</h2></div></div>
  <p style="font-size:0.82rem;color:var(--body);margin-top:-10px;margin-bottom:18px;max-width:760px;">Enable, disable, rename, or add requirements for future assessments. New requirements are treated as manual CVO verification until a matching automatic rule is implemented. Existing assessment results are preserved.</p>
  ${svcBlocks}`;
}
function editCriterionModal(svc, idx){
  const c = S.criteria[svc][idx];
  openModal(`
  <div class="modal-box">
    <h3>Edit Criterion</h3>
    <div class="field"><label>Service</label><input value="${esc(svc)}" disabled></div>
    <div class="field"><label>Requirement Label *</label><input id="crit-label" value="${esc(c.label)}"></div>
    ${field("Description *", `<textarea id="crit-desc" rows="3">${esc(c.desc)}</textarea>`)}
    <div class="modal-actions">
      <button class="btn-line" id="m-cancel">Cancel</button>
      <button class="btn-red" id="m-save-criterion" data-svc="${esc(svc)}" data-idx="${idx}">Save Changes</button>
    </div>
  </div>`);
}
function saveCriterion(svc, idx, label, desc){
  const c = S.criteria[svc][idx];
  runAction(() => Api.put("/api/admin/criteria", {service:svc, key:c.key, label, description:desc}),
    () => { closeModal(); showToast("Criterion updated."); });
}
function addCriterionModal(svc){openModal(`<div class="modal-box"><h3>Add Eligibility Requirement</h3><p class="form-note">This requirement will require manual CVO verification during future assessments.</p>${field("Service",`<input value="${esc(svc)}" disabled>`)}${field("Requirement label *",`<input id="new-crit-label">`)}${field("Instructions / description *",`<textarea id="new-crit-desc" rows="4"></textarea>`)}<div class="modal-actions"><button class="btn-line" id="m-cancel">Cancel</button><button class="btn-red" id="m-add-criterion" data-svc="${esc(svc)}">Add Requirement</button></div></div>`);}
function toggleCriterion(svc, idx){
  if(!AuthService.hasRole("admin")) return;   // convenience only: the server enforces this
  const c = S.criteria[svc][idx];
  const enable = !c.enabled;
  // The server applies this switch to every future eligibility assessment.
  runAction(() => Api.put("/api/admin/criteria", {service:svc, key:c.key, enabled:enable}),
    () => { showToast(`Criterion ${enable ? "enabled" : "disabled"}.`); render(); });
}
