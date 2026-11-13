/* PetCARE — API client
   ============================================================
   The only place the browser talks to the server. Same-origin requests carry the HttpOnly session cookie
   automatically; the page never sees a token or a password hash.
   Every request sends "X-Requested-With: PetCARE": the server refuses state-changing calls without it, which
   blocks forged requests from other websites (see RequestedWithFilter on the server).
   All calls resolve to {ok, status, data, error} and never throw. */
const Api = (function(){
  const AUTH_ENDPOINTS = /^\/api\/auth\/(login|signup|verify|resend-code|forgot|reset)/;

  async function request(method, url, body){
    const opts = { method, credentials:"same-origin", headers:{ "X-Requested-With":"PetCARE", "Accept":"application/json" } };
    if(body !== undefined){ opts.headers["Content-Type"] = "application/json"; opts.body = JSON.stringify(body); }
    let res;
    try{ res = await fetch(url, opts); }
    catch(e){ return { ok:false, status:0, data:null, error:"Cannot reach the server. Check your connection and try again." }; }
    let data = null;
    try{ data = await res.json(); }catch(e){ /* empty or non-JSON body */ }
    if(res.status === 401 && !AUTH_ENDPOINTS.test(url) && typeof onSessionLost === "function") onSessionLost();
    return {
      ok: res.ok, status: res.status, data,
      error: res.ok ? null : ((data && data.error) || "Something went wrong. Please try again."),
      code: data && data.code ? data.code : null,
    };
  }

  return {
    get:  url       => request("GET", url),
    post: (url, b)  => request("POST", url, b === undefined ? {} : b),
    put:  (url, b)  => request("PUT", url, b === undefined ? {} : b),
    delete: url      => request("DELETE", url),
  };
})();
