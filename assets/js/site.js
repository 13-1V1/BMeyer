(function(){
  var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* Scroll reveal */
  function initReveal(){
    var els = document.querySelectorAll('.reveal');
    if(reduce || !('IntersectionObserver' in window)){els.forEach(function(e){e.classList.add('in');});return;}
    var io = new IntersectionObserver(function(entries){
      entries.forEach(function(en){ if(en.isIntersecting){en.target.classList.add('in'); io.unobserve(en.target);} });
    },{threshold:.12});
    els.forEach(function(e){io.observe(e);});
  }

  /* Forward zoom: a category card expands into the room.
     The card is a real link; this is only the click enhancement. */
  function zoomInto(card){
    var href = card.getAttribute('data-href') || card.getAttribute('href'); if(!href) return;
    if(reduce){ window.location.href = href; return; }
    var accent = card.getAttribute('data-accent') || getComputedStyle(document.documentElement).getPropertyValue('--green');
    var r = card.getBoundingClientRect();
    try{ sessionStorage.setItem('zoomOrigin', JSON.stringify({top:r.top,left:r.left,width:r.width,height:r.height,accent:accent,href:location.pathname.split('/').pop()})); }catch(e){}
    var z = document.createElement('div'); z.className='zoomer'; z.style.setProperty('--zc',accent);
    z.style.top=r.top+'px'; z.style.left=r.left+'px'; z.style.width=r.width+'px'; z.style.height=r.height+'px';
    z.style.background=getComputedStyle(card).backgroundColor;
    var inner=document.createElement('div'); inner.className='zi'; inner.innerHTML=card.getAttribute('data-ico')||'';
    z.appendChild(inner); document.body.appendChild(z); z.getBoundingClientRect();
    requestAnimationFrame(function(){
      z.style.top='0px'; z.style.left='0px'; z.style.width='100vw'; z.style.height='100vh'; z.style.borderRadius='0';
      z.style.background='#0c0f14'; inner.style.transform='scale(3)'; inner.style.opacity='0';
    });
    setTimeout(function(){ window.location.href=href; },500);
  }

  /* Reverse zoom: room shrinks back down toward the origin box, then go home */
  function zoomBack(href){
    if(reduce){ window.location.href=href; return; }
    var origin=null; try{ origin=JSON.parse(sessionStorage.getItem('zoomOrigin')); }catch(e){}
    var accent=getComputedStyle(document.documentElement).getPropertyValue('--accent')||'#3ddc84';
    var z=document.createElement('div'); z.className='zoomer'; z.style.setProperty('--zc',accent);
    z.style.top='0px'; z.style.left='0px'; z.style.width='100vw'; z.style.height='100vh'; z.style.borderRadius='0';
    z.style.background='#0c0f14';
    document.body.appendChild(z); z.getBoundingClientRect();
    var t = origin || {top:window.innerHeight*0.4,left:window.innerWidth*0.4,width:230,height:150};
    requestAnimationFrame(function(){
      z.style.top=t.top+'px'; z.style.left=t.left+'px'; z.style.width=t.width+'px'; z.style.height=t.height+'px';
      z.style.borderRadius='14px'; z.style.opacity='0';
    });
    setTimeout(function(){ window.location.href=href; },480);
  }

  function initNav(){
    var inner = document.querySelector('.nav-inner');
    var links = inner && inner.querySelector('.nav-links');
    if(!inner || !links || inner.querySelector('.nav-toggle')) return;
    if(!links.id) links.id = 'primary-nav';
    var btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'nav-toggle';
    btn.setAttribute('aria-expanded','false');
    btn.setAttribute('aria-controls', links.id);
    btn.setAttribute('aria-label','Open menu');
    btn.innerHTML = '<span></span><span></span><span></span>';
    inner.appendChild(btn);
    function setOpen(open){
      inner.classList.toggle('nav-open', open);
      btn.setAttribute('aria-expanded', open ? 'true' : 'false');
      btn.setAttribute('aria-label', open ? 'Close menu' : 'Open menu');
    }
    btn.addEventListener('click', function(){ setOpen(!inner.classList.contains('nav-open')); });
    links.addEventListener('click', function(ev){ if(ev.target.closest('a')) setOpen(false); });
    document.addEventListener('keydown', function(ev){ if(ev.key === 'Escape') setOpen(false); });
    document.addEventListener('click', function(ev){ if(!inner.contains(ev.target)) setOpen(false); });
  }

  document.addEventListener('DOMContentLoaded',function(){
    initReveal();
    initNav();
    document.querySelectorAll('[data-href]').forEach(function(card){
      card.addEventListener('click',function(ev){
        // Let modified and non-primary clicks (new tab, new window) through.
        if(ev.metaKey || ev.ctrlKey || ev.shiftKey || ev.altKey || ev.button !== 0) return;
        var href = card.getAttribute('data-href') || card.getAttribute('href');
        if(!href || href.charAt(0) === '#') return;
        ev.preventDefault();
        zoomInto(card);
      });
    });
    document.querySelectorAll('[data-back]').forEach(function(a){
      a.addEventListener('click',function(ev){
        if(ev.metaKey || ev.ctrlKey || ev.shiftKey || ev.altKey || ev.button !== 0) return;
        ev.preventDefault();
        zoomBack(a.getAttribute('href'));
      });
    });
  });
})();
