/* PetCARE visual experience: persistent theme preference and accessible scroll reveals. */
let petcareRevealObserver = null;

function currentTheme(){
  return document.documentElement.dataset.theme === "dark" ? "dark" : "light";
}

function themeToggleButton(context){
  const dark = currentTheme() === "dark";
  const label = dark ? "Use light theme" : "Use dark theme";
  const icon = dark
    ? '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.42 1.42M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.42-1.42M17.66 6.34l1.41-1.41"/></svg>'
    : '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20.5 14.2A8.5 8.5 0 0 1 9.8 3.5 8.5 8.5 0 1 0 20.5 14.2Z"/></svg>';
  return `<button class="theme-toggle theme-toggle--${context}" type="button" data-theme-toggle aria-label="${label}" title="${label}">${icon}<span>${dark?"Light":"Dark"}</span></button>`;
}

function togglePetcareTheme(){
  const next = currentTheme() === "dark" ? "light" : "dark";
  document.documentElement.dataset.theme = next;
  localStorage.setItem("petcare-theme", next);
  render();
}

function bindModernExperience(root){
  root.querySelectorAll("[data-theme-toggle]").forEach(button=>button.addEventListener("click", togglePetcareTheme));

  if(petcareRevealObserver) petcareRevealObserver.disconnect();
  const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
  const candidates = [...root.querySelectorAll(".reveal, .main .page-head, .main .stat-card, .main .card, .main .pet-card, .main .report-card")];
  candidates.forEach((el,index)=>{
    el.classList.add("scroll-reveal");
    el.style.setProperty("--reveal-delay", `${Math.min(index%5,4)*55}ms`);
  });
  if(reduced || !("IntersectionObserver" in window)){
    candidates.forEach(el=>el.classList.add("is-visible"));
    return;
  }
  petcareRevealObserver = new IntersectionObserver(entries=>{
    entries.forEach(entry=>{
      if(entry.isIntersecting){
        entry.target.classList.add("is-visible");
        petcareRevealObserver.unobserve(entry.target);
      }
    });
  },{rootMargin:"0px 0px -7% 0px",threshold:0.08});
  candidates.forEach(el=>petcareRevealObserver.observe(el));
}

