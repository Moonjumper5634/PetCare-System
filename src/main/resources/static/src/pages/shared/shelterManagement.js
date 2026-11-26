/* PetCARE — Authorized shelter-dog listing management */
function screenShelterManagement(){
  const selected=S.shelterPets.find(p=>p.id===UI.selected.shelterId)||null;
  const value=(key,fallback="")=>esc(selected?.[key]??fallback);
  return `<div class="page-head"><div><div class="crumb">Adoption</div><h2>Shelter Dog Management</h2></div></div>
  <div class="role-banner"><span>Only CVO staff and administrators can create or change listings. Archived and unavailable dogs are hidden from residents.</span></div>
  <div class="two-col shelter-management-grid">
    <form class="card" id="shelter-pet-form"><h3>${selected?`Edit ${esc(selected.name)}`:"Add Shelter Dog"}</h3>
      <div class="two-field"><label class="field"><span>Dog Name *</span><input id="sp-name" required maxlength="80" value="${value('name')}"></label><label class="field"><span>Breed</span><input id="sp-breed" maxlength="80" value="${value('breed')}"></label></div>
      <div class="two-field"><label class="field"><span>Sex *</span><select id="sp-sex"><option ${value('sex','Male')==='Male'?'selected':''}>Male</option><option ${value('sex')==='Female'?'selected':''}>Female</option></select></label><label class="field"><span>Age</span><input id="sp-age" maxlength="40" placeholder="e.g. 2 years" value="${value('age')}"></label></div>
      <div class="two-field"><label class="field"><span>Status *</span><select id="sp-status">${['Available','Unavailable','Archived'].map(x=>`<option ${value('status','Available')===x?'selected':''}>${x}</option>`).join('')}</select><small>Application Pending and Adopted are set automatically by the adoption workflow.</small></label><label class="field"><span>Intake Date</span><input id="sp-intake" type="date" value="${value('intake',todayISO())}"></label></div>
      <label class="field"><span>Photo URL</span><input id="sp-photo" maxlength="500" value="${value('photo')}"></label>
      <label class="field"><span>Description</span><textarea id="sp-description" maxlength="1000">${value('description')}</textarea></label>
      <label class="field"><span>Temperament and Care Notes</span><textarea id="sp-temperament" maxlength="500">${value('temperament')}</textarea></label>
      <div class="form-actions">${selected?`<button type="button" class="btn-line" id="sp-cancel">Cancel Edit</button>`:""}<button class="btn-red" type="submit" data-id="${selected?.id||''}">${selected?'Save Changes':'Add Dog'}</button></div>
    </form>
    <div class="card"><h3>Current Shelter Dogs</h3>${S.shelterPets.length?S.shelterPets.map(p=>`<div class="checklist-row"><span><b>${esc(p.name)}</b> <small>${esc(p.id)}</small><br><small>${esc(p.breed||'Breed not recorded')} · ${esc(p.sex)} · ${esc(p.age||'Age not recorded')}</small></span><span>${statusPill(p.status)}<button class="btn-sm" data-nav="sheltermanagement" data-shelter="${p.id}">Edit</button></span></div>`).join(''):emptyState(icoSvg('folder'),"No Shelter Dogs","Add the first authorized adoption listing.","")}</div>
  </div>`;
}
