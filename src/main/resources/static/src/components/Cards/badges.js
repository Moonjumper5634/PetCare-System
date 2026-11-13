/* DogCARE — Status pills and pet avatar */

function pill(spec){
  const [type,label] = spec.split(":");
  return `<span class="pill ${type}">${label}</span>`;
}
function statusPill(status){
  const positive = ["Verified","Eligible","Approved","Completed","Adopted","Active","Passed"];
  const negative = ["Rejected","Not Eligible","Failed","Inactive"];
  if(positive.includes(status)) return pill("green:"+status);
  if(negative.includes(status)) return pill("red:"+status);
  return pill("amber:"+status);
}
function registrationStatusPill(status){
  if(status === "Pending") return pill("amber:Under verification");
  if(status === "Verified") return pill("green:Verified");
  if(status === "Rejected") return pill("red:Needs correction");
  return statusPill(status);
}

function petAvatar(p, size){
  size = size || 48;
  if(p.photo){
    return `<div class="pet-avatar" style="width:${size}px;height:${size}px;"><img src="${p.photo}" alt="${esc(p.name)}"></div>`;
  }
  return `<div class="pet-avatar" style="width:${size}px;height:${size}px;">${esc(initials(p.name))}</div>`;
}
