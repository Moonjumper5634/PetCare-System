/* PetCARE — Page: records */

/* ---------------- ADMIN: PET RECORDS / SERVICE MANAGEMENT ---------------- */
function screenPetRecordsAdmin(){
  const duplicateGroups=[],seen=new Set();
  S.pets.forEach(p=>{const key=`${p.ownerId}|${String(p.name).toLowerCase()}|${p.dob}`;const matches=S.pets.filter(x=>`${x.ownerId}|${String(x.name).toLowerCase()}|${x.dob}`===key||(p.microchip&&x.microchip===p.microchip));if(matches.length>1&&!seen.has(key)){duplicateGroups.push(matches);seen.add(key);}});
  const pg=paged(S.pets,"pets"),rows=pg.items;
  return `
  <div class="page-head"><div><div class="crumb">Administration</div><h2>Pet Records</h2></div></div>
  ${duplicateGroups.length?`<div class="security-notice duplicate-notice"><b>${duplicateGroups.length} possible duplicate group${duplicateGroups.length===1?'':'s'} need review.</b> PetCARE will not automatically merge or delete veterinary histories. Compare the owner, birth date and microchip before an authorized database correction.${duplicateGroups.map(g=>`<div>${g.map(p=>`${esc(p.id)} · ${esc(p.name)}`).join(' ↔ ')}</div>`).join('')}</div>`:''}
  <div class="card">
    <table><thead><tr><th>Pet ID</th><th>Name</th><th>Species</th><th>Breed</th><th>Owner</th><th>Barangay</th><th>Registration</th><th>Last Rabies Vax</th><th>Action</th></tr></thead>
    <tbody>${rows.map(p=>{const o=userById(p.ownerId);return `<tr><td>${p.id}</td><td><b>${esc(p.name)}</b></td><td>${esc(p.species)}</td><td>${esc(p.breed)}</td><td>${o?esc(o.name):"—"}</td><td>${esc(p.barangay)}</td><td>${registrationStatusPill(p.registrationStatus)}</td><td>${p.lastRabies?fmtDate(p.lastRabies):"No Record"}</td>
      <td><button class="btn-sm" data-nav="petprofile" data-pet="${p.id}">View</button></td></tr>`;}).join("")}</tbody></table>${pager(pg)}
  </div>`;
}
function screenServiceManagement(){
  const rows = [...S.requests].sort((a,b)=>b.submittedDate.localeCompare(a.submittedDate));
  return `
  <div class="page-head"><div><div class="crumb">Administration</div><h2>Service Management</h2></div></div>
  <div class="card">
    <table><thead><tr><th>Request ID</th><th>Pet</th><th>Owner</th><th>Service</th><th>Date</th><th>Status</th><th>Action</th></tr></thead>
    <tbody>${rows.map(r=>{const p=petById(r.petId), o=userById(r.ownerId);return `<tr><td>${r.id}</td><td>${p?esc(p.name):"—"}</td><td>${o?esc(o.name):"—"}</td><td>${esc(r.service)}</td><td>${fmtDate(r.submittedDate)}</td><td>${statusPill(requestDisplayStatus(r))}</td>
      <td><button class="btn-sm" data-nav="reqdetail" data-req="${r.id}">View</button></td></tr>`;}).join("")}</tbody></table>
  </div>`;
}
