/* DogCARE — Empty state and request/registration timelines */

/* ---------------- SHARED COMPONENTS ---------------- */
function emptyState(icon, title, msg, actionHtml){
  return `
  <div class="empty-state">
    <div class="icon">${icon}</div>
    <div style="font-weight:700;font-size:0.95rem;color:var(--text);margin-bottom:4px;">${title}</div>
    <div class="msg">${msg}</div>
    ${actionHtml||""}
  </div>`;
}

function renderTimeline(steps){
  return `<div class="timeline">${steps.map(s=>{
    const color = s.state==="done" ? "green" : s.state==="rejected" ? "red" : s.state==="current" ? "amber" : "gray";
    const mark = s.state==="done" ? "✓" : s.state==="rejected" ? "✕" : s.state==="current" ? "●" : "○";
    return `<div class="tl-step">
      <div class="tl-dot ${color}"></div>
      <div class="tl-title">${mark} ${esc(s.label)}</div>
      ${s.desc?`<div class="tl-desc">${esc(s.desc)}</div>`:""}
    </div>`;
  }).join("")}</div>`;
}

function registrationTimelineSteps(reg){
  const pet = petById(reg.petId);
  if(reg.status === "Verified"){
    return [
      {label:"Registered", state:"done", desc:`Submitted ${fmtDate(reg.submittedDate)}.`},
      {label:"Under verification", state:"done", desc:"Reviewed by CVO staff."},
      {label:"Verified", state:"done", desc:"Registration information was verified."},
      {label:"Verified Pet", state:"done", desc:`${pet?pet.name:"Pet"} is now a verified pet and eligible to request services.`},
    ];
  }
  if(reg.status === "Rejected"){
    return [
      {label:"Registered", state:"done", desc:`Submitted ${fmtDate(reg.submittedDate)}.`},
      {label:"Under verification", state:"done", desc:"Reviewed by CVO staff."},
      {label:"Needs correction", state:"rejected", desc: reg.rejectionReason ? `Correction needed: ${reg.rejectionReason}` : "The resident needs to correct the registration information."},
    ];
  }
  return [
    {label:"Registered", state:"done", desc:`Submitted ${fmtDate(reg.submittedDate)}.`},
    {label:"Under verification", state:"current", desc:"Awaiting review by CVO staff."},
    {label:"Verified or needs correction", state:"upcoming"},
    {label:"Verified Pet", state:"upcoming"},
  ];
}

function requestTimelineSteps(req){
  const steps = [];
  steps.push({label:"Submitted", state:"done", desc:`Submitted ${fmtDate(req.submittedDate)}.`});
  steps.push({label:"Initial Validation", state:"done", desc:"Request information validated."});
  if(!req.eligibility){
    steps.push({label:"Eligibility Assessment", state:"current", desc:"Awaiting eligibility assessment."});
    steps.push({label:"Staff Review", state:"upcoming"});
    steps.push({label:"Decision", state:"upcoming"});
    steps.push({label:"Service", state:"upcoming"});
    steps.push({label:"Completed", state:"upcoming"});
    return steps;
  }
  steps.push({label:"Eligibility Assessment", state:"done", desc:`System Recommendation: ${req.eligibility.eligible?"ELIGIBLE":"NOT ELIGIBLE"}.`});
  if(req.staffDecision === "Pending"){
    steps.push({label:"Staff Review", state:"current", desc:"Awaiting Final CVO Staff Decision."});
    steps.push({label:"Decision", state:"upcoming"});
    steps.push({label:"Service", state:"upcoming"});
    steps.push({label:"Completed", state:"upcoming"});
    return steps;
  }
  steps.push({label:"Staff Review", state:"done", desc:"Reviewed by CVO staff."});
  if(req.staffDecision === "Rejected"){
    steps.push({label:"Decision: Rejected", state:"rejected", desc: req.rejectionReason ? `Reason: ${req.rejectionReason}` : "Request was rejected."});
    return steps;
  }
  steps.push({label:"Decision: Approved", state:"done", desc:`Approved ${fmtDate(req.decisionDate)}.`});
  if(!req.serviceRecord){
    steps.push({label:"Service", state:"current", desc:"Awaiting service to be recorded."});
    steps.push({label:"Completed", state:"upcoming"});
    return steps;
  }
  steps.push({label:"Service", state:"done", desc:`Service performed ${fmtDate(req.serviceRecord.date)}.`});
  steps.push({label:"Completed", state:"done", desc:"Service record saved. Pet history updated."});
  return steps;
}
