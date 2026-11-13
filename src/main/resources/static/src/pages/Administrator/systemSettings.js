/* PetCARE — Page: systemSettings */

/* ---------------- ADMIN: SYSTEM SETTINGS ---------------- */
function screenSystemSettings(){
  const s=S.settings||{};
  return `
  <div class="page-head"><div><div class="crumb">Administration</div><h2>System Settings</h2></div></div>
  <div class="card" style="max-width:640px;">
    <h3>Organization Details</h3>
    <div class="field"><label for="settings-office">Office Name</label><input id="settings-office" maxlength="160" value="${esc(s.officeName||'')}"></div>
    <div class="field"><label for="settings-email">Contact Email</label><input id="settings-email" type="email" maxlength="190" value="${esc(s.contactEmail||'')}"></div>
    <div class="field"><label for="settings-phone">Contact Number</label><input id="settings-phone" maxlength="30" value="${esc(s.contactNumber||'')}"></div>
    <button class="btn-red" id="settings-save">Save Changes</button>
  </div>
  <div class="card" style="max-width:640px;">
    <h3>Notification Settings</h3>
    <label class="setting-toggle"><span>Send status update notifications to residents</span><input id="settings-status" type="checkbox" ${s.statusNotifications?'checked':''}></label>
    <label class="setting-toggle"><span>Send vaccination due-date reminders</span><input id="settings-reminders" type="checkbox" ${s.vaccinationReminders?'checked':''}></label>
    <label class="setting-toggle"><span>Send monthly summary report to admins</span><input id="settings-summary" type="checkbox" ${s.monthlyAdminSummary?'checked':''}></label>
  </div>`;
}
