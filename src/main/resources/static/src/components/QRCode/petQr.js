/* PetCARE — Digital Pet ID: QR payload, rendering and parsing */

/* ---------------- DIGITAL PET ID / QR CODE ----------------
   The QR encodes a human-readable snapshot of the pet's record directly, so
   scanning it with any ordinary camera app (Google Lens, phone camera, etc.)
   shows real, useful information immediately — not just an opaque code.
   A machine-parseable "Scan Code" line is also embedded in the same text so
   PetCARE's own in-app scanner can pull the exact Pet ID back out and jump
   straight to that pet's full record. simpleHash() is a lightweight checksum
   for this prototype only — a production system would use a signed/
   server-verified token, and would likely also encode a live URL so the
   record always reflects the most current data rather than a snapshot. */
function simpleHash(str){
  let h = 5381;
  for(let i=0;i<str.length;i++){ h = ((h<<5)+h) + str.charCodeAt(i); h = h & 0xffffffff; }
  return Math.abs(h).toString(36).toUpperCase().slice(0,6);
}
function petQrValue(pet){
  const owner = userById(pet.ownerId);
  const token = simpleHash(pet.id+"|SANTAROSA-CVO");
  const lines = [
    "PetCARE Digital Pet ID",
    "City Veterinary Office — Santa Rosa, Laguna",
    "",
    `Pet: ${pet.name} (${pet.id})`,
    `Breed: ${pet.breed || "—"}  •  Sex: ${pet.sex || "—"}  •  Species: ${pet.species || "Dog"}`,
    `Owner: ${owner ? owner.name : "—"}`,
    `Barangay: ${pet.barangay || "—"}`,
    `Registration Status: ${pet.registrationStatus || "—"}`,
    `Last Anti-Rabies Vaccination: ${pet.lastRabies ? fmtDate(pet.lastRabies) : "No record on file"}`,
    `Deworming Status: ${pet.dewormingStatus || "Unknown"}`,
    `Microchip: ${pet.microchipStatus === "Microchipped" ? ("#"+pet.microchip) : "Not microchipped"}`,
    "",
    "This is a snapshot for reference only. For the current record, staff",
    "should look up this pet inside PetCARE using the code below.",
    `Scan Code: PETCARE:${pet.id}:${token}`,
  ];
  return lines.join("\n");
}
function parsePetQrValue(text){
  if(!text) return null;
  const m = String(text).match(/PETCARE:([A-Za-z0-9\-]+):([A-Za-z0-9]+)/);
  if(!m) return null;
  const [, petId, token] = m;
  if(token !== simpleHash(petId+"|SANTAROSA-CVO")) return null;
  return petId;
}
function makeQrSvg(text, cellSize, margin){
  if(typeof qrcode === "undefined") return "";
  cellSize = cellSize || 4;
  margin = margin == null ? 8 : margin;
  var qr = null;
  for(var type = 1; type <= 40; type++){
    try{ qr = qrcode(type, "M"); qr.addData(text); qr.make(); break; }
    catch(e){ qr = null; }
  }
  if(!qr) return "";
  return qr.createSvgTag(cellSize, margin);
}
function renderPetQrInto(containerId, pet){
  const el = document.getElementById(containerId);
  if(!el) return;
  const svg = makeQrSvg(petQrValue(pet), 4, 8);
  if(!svg){
    el.innerHTML = `<div style="font-size:0.68rem;color:var(--muted);padding:20px;text-align:center;">QR unavailable in this browser.<br>Pet ID: <b>${esc(pet.id)}</b></div>`;
    return;
  }
  el.innerHTML = svg;
  const svgEl = el.querySelector("svg");
  if(svgEl){ svgEl.style.width = "100%"; svgEl.style.height = "100%"; svgEl.style.display = "block"; }
}
