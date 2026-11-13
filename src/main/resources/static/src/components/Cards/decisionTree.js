/* PetCARE — Page: decisionTree */

/* ---------------- REQUEST DETAIL (Eligibility + Staff Decision + Timeline) ---------------- */
function renderDecisionTree(decisionPath){
  return `<div class="dtree">${decisionPath.map((n,i)=>{
    const isLast = i===decisionPath.length-1;
    let badgeClass, badgeText;
    if(n.type==="start"){ badgeClass="start"; badgeText="•"; }
    else if(n.type==="final"){ badgeClass = n.value==="ELIGIBLE"?"pass":n.value==="NOT ELIGIBLE"?"fail":"review"; badgeText="=="; }
    else { badgeClass = n.result==="PASS"?"pass":n.result==="FAIL"?"fail":"review"; badgeText = n.result==="PASS"?"✓":n.result==="FAIL"?"✕":"?"; }
    const node = `
    <div class="dtree-node">
      <div class="dtree-badge ${badgeClass}">${badgeText}</div>
      <div class="dtree-body">
        <span class="dtree-label">${esc(n.label)}</span>${n.result?`<span class="dtree-result ${n.result==="PASS"?"pass":n.result==="FAIL"?"fail":"review"}">${n.result}</span>`:""}
        ${n.value!=null ? `<div class="dtree-value">Registered value: ${esc(String(n.value))}</div>` : ""}
        ${n.basis ? `<div class="dtree-basis">${esc(n.basis)}</div>` : ""}
      </div>
    </div>`;
    return node + (isLast?"":`<div class="dtree-connector"></div>`);
  }).join("")}</div>`;
}
