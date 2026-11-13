/* DogCARE — public Phase 4 landing page */
function landingIcon(name){
  const paths = {
    vaccine:'<path d="M14 4l6 6"/><path d="M17 3l4 4"/><path d="M5 13l6 6"/><path d="M4 20l4-1 10-10-3-3L5 16l-1 4z"/><path d="M8 12l4 4"/>',
    chip:'<rect x="7" y="7" width="10" height="10" rx="2"/><path d="M9 1v3M15 1v3M9 20v3M15 20v3M1 9h3M1 15h3M20 9h3M20 15h3"/><circle cx="12" cy="12" r="2"/>',
    care:'<path d="M12 21s-7-4.4-7-10a4 4 0 017-2.6A4 4 0 0119 11c0 5.6-7 10-7 10z"/><path d="M9 13h6M12 10v6"/>',
    register:'<path d="M5 3h14v18H5z"/><path d="M9 3V1h6v2M8 8h8M8 12h5M8 16h4"/>',
    verify:'<path d="M12 22a10 10 0 100-20 10 10 0 000 20z"/><path d="M7.5 12l3 3 6-7"/>',
    request:'<path d="M4 4h16v16H4z"/><path d="M8 9h8M8 13h5"/><path d="M16 17l2 2 3-4"/>',
    track:'<path d="M3 12h4l2-5 4 10 2-5h6"/>',
  };
  return `<svg viewBox="0 0 24 24" aria-hidden="true">${paths[name]||paths.care}</svg>`;
}

function renderLandingPage(){
  return `
  <div class="landing" id="top">
    <header class="landing-nav" aria-label="Public navigation">
      <a class="landing-brand" href="#top" aria-label="DogCARE home">${sealSvg(42)}<span><b>DogCARE</b><small>City Veterinary Office · Santa Rosa, Laguna</small></span></a>
      <nav class="landing-links" aria-label="Page sections">
        <a href="#services">Services</a><a href="#process">How it works</a><a href="#community">Community</a><a href="#contact">Contact</a>
      </nav>
      <div class="landing-actions">${themeToggleButton("landing")}<button class="btn-line landing-login" data-auth="login">Sign in</button><button class="btn-red" data-auth="signup">Create account</button></div>
      <button class="landing-menu" id="landing-menu" aria-label="Open navigation" aria-expanded="false">☰</button>
    </header>

    <main>
      <section class="landing-hero">
        <div class="hero-copy reveal">
          <div class="eyebrow">A digital service for Santa Rosa dog owners</div>
          <h1>Care for your dog,<br><span>closer to home.</span></h1>
          <p>Register your dog, request available veterinary services, and follow every update from submission to completion through one secure portal.</p>
          <div class="hero-actions"><button class="btn-red btn-lg" data-auth="signup">Register as a resident</button><a class="btn-line btn-lg" href="#services">Explore services</a></div>
          <div class="hero-trust"><span>✓ Canine-focused</span><span>✓ CVO-managed records</span><span>✓ Mobile-friendly</span></div>
        </div>
        <div class="hero-visual reveal" aria-label="DogCARE dog service illustration">
          <div class="hero-orbit orbit-one"></div><div class="hero-orbit orbit-two"></div>
          <svg class="dog-illustration" viewBox="0 0 430 390" role="img" aria-label="A cared-for dog wearing a DogCARE tag">
            <path fill="#f5ded7" d="M88 312c15-62 53-95 98-103-14-33-1-78 35-94 28-13 63-6 81 14 20-24 59-34 87-14 33 23 39 69 17 101 22 26 29 60 24 96H88z"/>
            <path fill="#c77b52" d="M166 116c-27-52-74-55-83-29-8 25 22 70 71 83m146-52c29-51 75-51 83-24 7 25-25 68-73 78"/>
            <path fill="#d89466" d="M151 178c0-68 41-111 101-111s103 44 103 112c0 69-45 119-102 119s-102-49-102-120z"/>
            <path fill="#fff4e8" d="M195 218c4-30 27-48 58-48s54 18 58 48c4 32-20 62-58 62s-62-30-58-62z"/>
            <circle cx="211" cy="167" r="8" fill="#292421"/><circle cx="295" cy="167" r="8" fill="#292421"/>
            <path d="M242 210c7-7 16-7 23 0-1 13-22 13-23 0z" fill="#292421"/><path d="M253 222c0 13-11 20-22 13m22-13c0 13 11 20 22 13" fill="none" stroke="#292421" stroke-width="5" stroke-linecap="round"/>
            <path d="M181 265c40 28 104 28 144 0" fill="none" stroke="#8b1e2e" stroke-width="14" stroke-linecap="round"/>
            <path fill="#8b1e2e" d="M231 278h44l-5 50-17 13-17-13z"/><path fill="#fff" d="M248 294h10v24h-10zM241 301h24v10h-24z"/>
          </svg>
          <div class="visual-card visual-card-status"><span class="status-dot"></span><div><small>Registration status</small><b>Under verification</b></div></div>
          <div class="visual-card visual-card-service">${landingIcon('vaccine')}<div><small>Available service</small><b>Anti-rabies vaccination</b></div></div>
        </div>
      </section>

      <section class="landing-note reveal" aria-label="Registration information">
        <div class="note-icon">i</div><div><b>Your dog is registered when you submit its information.</b><p>Its status will show <strong>Registered — Under verification</strong> while authorized CVO staff check the record. Services that require verified registration become available after verification.</p></div>
      </section>

      <section class="landing-section" id="services">
        <div class="section-heading reveal"><div><span class="eyebrow">Services supported by DogCARE</span><h2>Start with the care your dog needs</h2></div><p>Availability and eligibility are confirmed by the City Veterinary Office after your dog's information is reviewed.</p></div>
        <div class="service-grid">
          <article class="service-card reveal"><div class="service-icon">${landingIcon('vaccine')}</div><span class="service-number">01</span><h3>Anti-rabies vaccination</h3><p>Submit a request and follow its assessment, decision, and completed service record.</p><button data-auth="signup">Register to request <span>→</span></button></article>
          <article class="service-card reveal"><div class="service-icon">${landingIcon('chip')}</div><span class="service-number">02</span><h3>Microchipping</h3><p>Request pet identification support and keep the recorded microchip number with your dog's profile.</p><button data-auth="signup">Register to request <span>→</span></button></article>
          <article class="service-card reveal"><div class="service-icon">${landingIcon('care')}</div><span class="service-number">03</span><h3>Kapon / spay-neuter</h3><p>View the eligibility assessment and requirements before CVO staff record a final decision.</p><button data-auth="signup">Check the process <span>→</span></button></article>
        </div>
      </section>

      <section class="process-section" id="process">
        <div class="process-copy reveal"><span class="eyebrow light">A clear public-service process</span><h2>Know what happens next.</h2><p>DogCARE separates registration, verification, service eligibility, the staff decision, and actual service completion so every status has a clear meaning.</p><button class="btn-light" data-auth="signup">Create resident account</button></div>
        <ol class="process-list">
          <li class="reveal"><span>1</span><div>${landingIcon('register')}<h3>Register your dog</h3><p>Submit dog, owner, health, and residency information.</p></div></li>
          <li class="reveal"><span>2</span><div>${landingIcon('verify')}<h3>CVO verifies the record</h3><p>The registration remains visible as under verification until reviewed.</p></div></li>
          <li class="reveal"><span>3</span><div>${landingIcon('request')}<h3>Request a service</h3><p>Choose an available service for a verified dog.</p></div></li>
          <li class="reveal"><span>4</span><div>${landingIcon('track')}<h3>Track every update</h3><p>See the assessment, staff decision, and completed service record.</p></div></li>
        </ol>
      </section>

      <section class="landing-section community-section" id="community">
        <div class="section-heading reveal"><div><span class="eyebrow">CVO in the community</span><h2>Local care, documented clearly</h2></div><p>Santa Rosa CVO conducts barangay-based anti-rabies activities. DogCARE complements this field work with registration and service records residents can follow online.</p></div>
        <div class="community-layout">
          <div class="community-feature reveal"><div class="community-art"><span class="community-date">Barangay service</span>${landingIcon('vaccine')}</div><div><span class="eyebrow">Community vaccination</span><h3>Anti-rabies services where residents live</h3><p>Monthly schedules and field activities remain CVO announcements. DogCARE helps residents keep their dog information and completed service history together.</p><a href="https://www.facebook.com/Santarosacityvet" target="_blank" rel="noopener">View official CVO updates ↗</a></div></div>
          <aside class="community-facts reveal"><h3>Before requesting a service</h3><ul><li>Use an active resident account.</li><li>Register your dog and wait for verification.</li><li>Keep health and vaccination details accurate.</li><li>Follow CVO instructions for the selected service.</li></ul><p class="fact-note">Schedules and requirements can change. Confirm current announcements with the CVO.</p></aside>
        </div>
      </section>

      <section class="landing-cta reveal"><div><span class="eyebrow light">Ready to use DogCARE?</span><h2>Keep your dog's care journey in one place.</h2><p>Create a resident account, register your dog, and follow updates from the City Veterinary Office.</p></div><div><button class="btn-light" data-auth="signup">Create account</button><button class="btn-ghost-light" data-auth="login">Sign in</button></div></section>
    </main>

    <footer class="landing-footer" id="contact"><div class="footer-brand">${sealSvg(40)}<div><b>DogCARE</b><small>City Veterinary Office · Santa Rosa, Laguna</small></div></div><div><b>Office</b><span>Rizal Blvd., Barangay Market Area<br>Santa Rosa, Laguna</span></div><div><b>Contact</b><span>(049) 530-0015<br>Follow CVO for current schedules</span></div><div><b>Portal</b><button data-auth="login">Resident and personnel sign in</button></div></footer>
  </div>`;
}
