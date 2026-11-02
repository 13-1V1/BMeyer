// Created by: Brennan Meyer with use of Claude Code 11/02/2026 Santa Clarita, CA
// brennanmmeyer@gmail.com
//
// Click-to-enlarge for photo tiles. Adapted from the BEZ Works lightbox.
// Any image inside .proj-imgs or .study-gallery becomes a button that opens
// the full-size copy (data-full, else src). Arrows step through the same
// gallery; Escape, the X, or a click on the backdrop closes it.
(function(){
  var groups = document.querySelectorAll('.proj-imgs, .study-gallery');
  if (!groups.length) return;

  var box = document.createElement('div');
  box.className = 'lightbox';
  box.hidden = true;
  box.setAttribute('role', 'dialog');
  box.setAttribute('aria-modal', 'true');
  box.setAttribute('aria-label', 'Photo viewer');
  box.innerHTML =
    '<button type="button" class="lightbox-close" aria-label="Close">&times;</button>' +
    '<button type="button" class="lightbox-nav prev" aria-label="Previous photo">&#8249;</button>' +
    '<figure class="lightbox-figure"><img alt=""><figcaption></figcaption></figure>' +
    '<button type="button" class="lightbox-nav next" aria-label="Next photo">&#8250;</button>';
  document.body.appendChild(box);

  var img = box.querySelector('img'), cap = box.querySelector('figcaption');
  var prev = box.querySelector('.prev'), next = box.querySelector('.next'), close = box.querySelector('.lightbox-close');
  var shots = [], i = 0, opener = null;

  function label(el){
    var fig = el.closest('figure');
    var fc = fig && fig.querySelector('figcaption');
    return fc ? fc.textContent : el.alt;
  }
  function show(){
    var s = shots[i];
    img.src = s.getAttribute('data-full') || s.currentSrc || s.src;
    img.alt = s.alt;
    cap.textContent = label(s) + (shots.length > 1 ? '  ·  ' + (i + 1) + ' / ' + shots.length : '');
    prev.hidden = next.hidden = shots.length < 2;
  }
  function open(list, start, btn){
    shots = list; i = start; opener = btn;
    show();
    box.hidden = false;
    document.body.style.overflow = 'hidden';
    close.focus();
  }
  function shut(){
    box.hidden = true;
    document.body.style.overflow = '';
    img.removeAttribute('src');
    if (opener) opener.focus();
  }
  function step(d){ i = (i + d + shots.length) % shots.length; show(); }

  groups.forEach(function(g){
    var list = Array.prototype.slice.call(g.querySelectorAll('img'));
    list.forEach(function(el, n){
      var btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'lb-thumb';
      btn.setAttribute('aria-label', 'Enlarge: ' + label(el));
      el.parentNode.insertBefore(btn, el);
      btn.appendChild(el);
      btn.addEventListener('click', function(){ open(list, n, btn); });
    });
  });

  close.addEventListener('click', shut);
  prev.addEventListener('click', function(){ step(-1); });
  next.addEventListener('click', function(){ step(1); });
  box.addEventListener('click', function(e){ if (e.target === box) shut(); });
  document.addEventListener('keydown', function(e){
    if (box.hidden) return;
    if (e.key === 'Escape') shut();
    else if (e.key === 'ArrowLeft') step(-1);
    else if (e.key === 'ArrowRight') step(1);
    else if (e.key === 'Tab') {
      // keep focus inside the viewer while it is open
      var f = [close, prev, next].filter(function(b){ return !b.hidden; });
      var k = f.indexOf(document.activeElement);
      e.preventDefault();
      f[(k + (e.shiftKey ? -1 : 1) + f.length) % f.length].focus();
    }
  });

  // swipe left/right on phones
  var x0 = null;
  box.addEventListener('touchstart', function(e){ x0 = e.touches[0].clientX; }, { passive: true });
  box.addEventListener('touchend', function(e){
    if (x0 === null) return;
    var dx = e.changedTouches[0].clientX - x0;
    if (Math.abs(dx) > 50 && shots.length > 1) step(dx < 0 ? 1 : -1);
    x0 = null;
  });
})();
