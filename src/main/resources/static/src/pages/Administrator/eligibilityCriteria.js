/* PetCARE — Page: eligibilityCriteria */

/* ---------------- ADMIN: ELIGIBILITY CRITERIA ---------------- */
function screenEligibilityCriteria(){
  const svcBlocks = Object.keys(S.criteria).map(svc=>{
    const items = S.criteria[svc];
    return `
    <div class="card">
      <h3>${esc(svc)}</h3>
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
  <p style="font-size:0.82rem;color:var(--body);margin-top:-10px;margin-bottom:18px;max-width:720px;">These criteria describe the configured eligibility rules used by CVO staff during assessment. Edits are recorded to the audit log.</p>
  ${svcBlocks}`;
}
function editCriterionModal(svc, idx){
  const c = S.criteria[svc][idx];
  openModal(`
  <div class="modal-box">
    <h3>Edit Criterion</h3>
    <div class="field"><label>Service</label><input value="${esc(svc)}" disabled></div>
    <div class="field"><label>Criterion Label</label><input value="${esc(c.label)}" disabled></div>
    ${field("Description *", `<textarea id="crit-desc" rows="3">${esc(c.desc)}</textarea>`)}
    <div class="modal-actions">
      <button class="btn-line" id="m-cancel">Cancel</button>
      <button class="btn-red" id="m-save-criterion" data-svc="${esc(svc)}" data-idx="${idx}">Save Changes</button>
    </div>
  </div>`);
}
function saveCriterion(svc, idx, desc){
  const c = S.criteria[svc][idx];
  runAction(() => Api.put("/api/admin/criteria", {service:svc, key:c.key, description:desc}),
    () => { closeModal(); showToast("Criterion updated."); });
}
function toggleCriterion(svc, idx){
  if(!AuthService.hasRole("admin")) return;   // convenience only: the server enforces this
  const c = S.criteria[svc][idx];
  const enable = !c.enabled;
  // The server applies this switch to every future eligibility assessment.
  runAction(() => Api.put("/api/admin/criteria", {service:svc, key:c.key, enabled:enable}),
    () => { showToast(`Criterion ${enable ? "enabled" : "disabled"}.`); render(); });
}
