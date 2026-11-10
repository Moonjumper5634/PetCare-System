/* PetCARE — Page: faq */

function screenFAQ(){
  const items = [
    {q:"What services does PetCARE cover?", a:"PetCARE currently supports three canine veterinary services: Anti-Rabies Vaccination, Microchipping, and Kapon / Spay-Neuter. Deworming is tracked as a health record on the pet profile, not as a separately requestable service."},
    {q:"Is PetCARE for dogs and cats?", a:"PetCARE is a canine-focused system. Only dogs can currently be registered and requested for service."},
    {q:"How do I register my dog?", a:"Go to Register a Pet from the sidebar, complete the dog and owner information, then the health and requirement information (weight, health status, anti-rabies status, deworming status, microchip status, proof of residency), and submit for CVO verification."},
    {q:"How is my eligibility for a service decided?", a:"Each service has a fixed set of decision-tree criteria. When a request is assessed, every criterion is evaluated as Passed, Failed, or Needs Verification. If any criterion fails, the classification is Not Eligible. If none fail but at least one needs verification, the classification is For Review. If every criterion passes, the classification is Eligible. This is a recommendation only — the final decision always rests with CVO staff."},
    {q:"What does 'For Review' mean?", a:"For Review means no requirement was clearly failed, but at least one requirement (for example, weight or deworming status) is missing or unconfirmed. CVO staff may request additional verification from the resident before making a final decision."},
    {q:"What are the Kapon / Spay-Neuter requirements?", a:"The dog must be registered and verified, 5 months to 4 years old, meet the weight requirement, be recorded as apparently healthy, have an updated anti-rabies vaccination, have an updated deworming record, and have a verified owner proof of residency."},
    {q:"How does pet adoption work?", a:"After submitting an adoption application, CVO staff will conduct an interview to review your household situation and ability to care for the pet. Once the interview is completed, staff will record a final decision to approve or reject the application."},
    {q:"Who do I contact for help?", a:"You may reach the City Veterinary Office — Santa Rosa, Laguna at cvo@santarosalaguna.gov.ph or (049) 123 4567 during office hours."},
  ];
  return `
  <div class="page-head"><div><div class="crumb">Support</div><h2>FAQ / Help</h2></div></div>
  <p style="font-size:0.82rem;color:var(--body);margin-top:-10px;margin-bottom:18px;max-width:680px;">Answers to common questions about PetCARE. This page replaces a live chat assistant — for anything not covered here, please contact the City Veterinary Office directly.</p>
  <div class="card" style="max-width:760px;">
    ${items.map(it=>`
      <div style="border-bottom:1px solid var(--border);padding:14px 0;">
        <div style="font-weight:700;font-size:0.88rem;color:var(--text);margin-bottom:6px;">${esc(it.q)}</div>
        <div style="font-size:0.82rem;color:var(--body);line-height:1.5;">${esc(it.a)}</div>
      </div>`).join("")}
  </div>`;
}
