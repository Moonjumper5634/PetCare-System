/* DogCARE — Page: adoptionReview */

/* ---------------- STAFF: ADOPTION APPLICATIONS ---------------- */
function screenAdoptionApplications(){
  const rows = [...S.adoptions].sort((a,b)=>b.submittedDate.localeCompare(a.submittedDate));
  return `
  <div class="page-head"><div><div class="crumb">Adoption</div><h2>Adoption Applications</h2></div></div>
  <div class="card">
    ${rows.length ? `<table><thead><tr><th>Application ID</th><th>Applicant</th><th>Pet</th><th>Barangay</th><th>Date</th><th>Status</th><th>Action</th></tr></thead>
    <tbody>${rows.map(a=>`<tr><td>${a.id}</td><td>${esc(a.applicantName)}</td><td>${shelterById(a.shelterPetId)?.name||"—"}</td><td>${esc(a.barangay)}</td><td>${fmtDate(a.submittedDate)}</td><td>${statusPill(a.status)}</td>
      <td><button class="btn-sm solid" data-nav="adoptreview" data-adopt="${a.id}">Review</button></td></tr>`).join("")}</tbody></table>`
    : emptyState(icoSvg('doc'),"No Adoption Applications","Adoption applications will appear here once submitted.","")}
  </div>`;
}
function screenAdoptReview(){
  const a = adoptById(UI.selected.adoptId);
  if(!a) return emptyState(icoSvg('doc'),"No Application Selected","Choose an application to review.","");
  const d = shelterById(a.shelterPetId);
  return `
  <div class="page-head"><div><div class="crumb">Adoption Applications</div><h2>${a.id}</h2></div></div>
  <div class="two-col">
    <div class="col-stack">
      <div class="card"><h3>Applicant Information</h3>
        <div class="kv">
          <div><div class="k">Name</div><div class="v">${esc(a.applicantName)}</div></div>
          <div><div class="k">Contact</div><div class="v">${esc(a.contact)}</div></div>
          <div><div class="k">Email</div><div class="v">${esc(a.email)}</div></div>
          <div><div class="k">Address</div><div class="v">${esc(a.address)}, ${esc(a.barangay)}</div></div>
          <div><div class="k">Residency</div><div class="v">${a.barangay==="Outside Santa Rosa"?"Nonresident — eligible for staff review":"Santa Rosa / confirm during interview"}</div></div>
          <div><div class="k">Housing Type</div><div class="v">${esc(a.housing)}</div></div>
        </div>
      </div>
      <div class="card"><h3>Pet Information</h3>
        <div class="kv">
          <div><div class="k">Pet Name</div><div class="v">${esc(d.name)}</div></div>
          <div><div class="k">Breed</div><div class="v">${esc(d.breed)}</div></div>
          <div><div class="k">Status</div><div class="v">${statusPill(d.status)}</div></div>
        </div>
      </div>
      <div class="card"><h3>Application Answers</h3>
        <p style="font-size:0.82rem;"><b>Reason for Adoption:</b> ${esc(a.reason)}</p>
        <p style="font-size:0.82rem;"><b>Previous Pet Experience:</b> ${esc(a.experience||"—")}</p>
      </div>
      ${a.interview ? `<div class="card"><h3>Adoption Interview Record</h3>
        <div class="kv" style="margin-bottom:10px;">
          <div><div class="k">Conducted</div><div class="v">${fmtDate(a.interview.date)}</div></div>
          <div><div class="k">Staff Recommendation</div><div class="v">${esc(a.interview.recommendation)}</div></div>
        </div>
        <p style="font-size:0.82rem;"><b>Checklist:</b> ${a.interview.checklist.join(", ")}</p>
        <p style="font-size:0.82rem;"><b>Interview Notes:</b> ${esc(a.interview.notes||"—")}</p>
      </div>` : ""}
    </div>
    <div class="col-stack">
      <div class="card"><h3>Status</h3>${statusPill(a.status)}
        ${a.status==="Rejected" ? `<p style="font-size:0.8rem;margin-top:10px;">Reason: ${esc(a.rejectionReason||"—")}</p>` : ""}
      </div>
      ${a.status==="Interview Required" ? `
      <div class="card"><h3>Adoption Interview</h3>
        <p style="font-size:0.78rem;color:var(--body);margin-top:-6px;">Conduct the applicant interview before a final adoption decision can be recorded.</p>
        <div class="checklist-group" style="margin-bottom:10px;">
          ${["Applicant identity and address verified","Dog origin and applicant location reviewed","Home/housing situation discussed","Ability to provide long-term care discussed","Experience with pets discussed","Other household members/pets discussed"].map((it,i)=>`
            <label style="display:flex;gap:8px;align-items:flex-start;font-size:0.8rem;margin-bottom:6px;">
              <input type="checkbox" class="interview-check" value="${esc(it)}" style="margin-top:3px;"> ${esc(it)}
            </label>`).join("")}
        </div>
        ${field("Interview Notes", `<textarea id="interview-notes" rows="3" placeholder="Summary of the interview..."></textarea>`)}
        ${field("Staff Recommendation *", `<select id="interview-recommendation"><option>Recommended</option><option>Not Recommended</option><option>Further Review</option></select>`)}
        <button class="btn-red" style="width:100%;margin-top:6px;" id="interview-complete-btn">Complete Interview</button>
      </div>` : ""}
      ${a.status==="Interview Completed" ? `
      <div class="card"><h3>Staff Remarks</h3>
        <p style="font-size:0.78rem;color:var(--body);">Interview recommendation: <b>${esc(a.interview?a.interview.recommendation:"—")}</b></p>
        <div class="field"><textarea id="adopt-remarks" rows="3" placeholder="Optional final remarks..."></textarea></div>
        <div style="display:flex;gap:10px;">
          <button class="btn-red" style="flex:1;" id="adopt-approve-btn">Approve Application</button>
          <button class="btn-sm danger" style="flex:1;padding:10px;" id="adopt-reject-btn">Reject Application</button>
        </div>
      </div>` : ""}
    </div>
  </div>`;
}
function completeInterview(adoptId){
  const notes = document.getElementById("interview-notes")?.value.trim() || "";
  const recommendation = document.getElementById("interview-recommendation")?.value || "Further Review";
  const checklist = [...document.querySelectorAll(".interview-check:checked")].map(el=>el.value);
  runAction(() => Api.post(`/api/adoptions/${encodeURIComponent(adoptId)}/interview`, {notes, recommendation, checklist}),
    () => { showToast(`Interview recorded for ${adoptId}.`); render(); });
}
function approveAdoption(adoptId){
  const a = adoptById(adoptId), d = shelterById(a.shelterPetId);
  runAction(() => Api.post(`/api/adoptions/${encodeURIComponent(adoptId)}/approve`),
    () => { showToast(`Adoption application ${adoptId} approved. ${d.name} is now marked Adopted.`); render(); });
}
function rejectAdoptionModal(adoptId){
  openModal(`
  <div class="modal-box">
    <h3>Reject Adoption Application?</h3>
    ${field("Reason for Rejection *", `<textarea id="adopt-reject-reason" rows="3"></textarea>`)}
    <div class="modal-actions">
      <button class="btn-line" id="m-cancel">Cancel</button>
      <button class="btn-sm danger" style="flex:1;padding:10px;" id="m-confirm-reject-adopt">Confirm Rejection</button>
    </div>
  </div>`);
  UI._pendingAdoptId = adoptId;
}
function confirmRejectAdoption(reason){
  if(!reason || !reason.trim()){ showToast("Please enter a rejection reason."); return; }
  const adoptId = UI._pendingAdoptId;
  runAction(() => Api.post(`/api/adoptions/${encodeURIComponent(adoptId)}/reject`, {reason:reason.trim()}),
    () => { closeModal(); showToast(`Adoption application ${adoptId} rejected.`); });
}
