// Created by: Brennan Meyer with use of Claude Code 11/02/2026 Santa Clarita, CA
// brennanmmeyer@gmail.com
//
// Lists the BEZ Works apps that belong to this room. Each program in BEZ Works'
// programs.json can carry "rooms": ["manufacturing", ...]; a page opts in with
// <div class="bez-apps" data-room="manufacturing"></div>. If the list can't be
// loaded, the plain BEZ Works link inside the box stays as it is.
(function(){
  var BEZ = 'https://13-1v1.github.io/BEZ-Works/';
  var boxes = document.querySelectorAll('.bez-apps[data-room]');
  if (!boxes.length || !window.fetch) return;

  function firstSentence(text){
    var m = String(text || '').match(/^.*?[.!?](?=\s|$)/);
    return m ? m[0] : String(text || '');
  }
  function el(tag, cls, text){
    var e = document.createElement(tag);
    if (cls) e.className = cls;
    if (text) e.textContent = text;
    return e;
  }

  fetch(BEZ + 'programs.json', { cache: 'no-cache' })
    .then(function(r){ if (!r.ok) throw new Error(r.status); return r.json(); })
    .then(function(data){
      var programs = (data && data.programs) || [];
      boxes.forEach(function(box){
        var room = box.getAttribute('data-room');
        var mine = programs.filter(function(p){
          return p.status !== 'coming-soon' && (p.rooms || []).indexOf(room) !== -1;
        });
        if (!mine.length) return;
        var list = el('ul', 'bez-apps-list');
        mine.forEach(function(p){
          var li = el('li');
          var a = el('a', null, p.name);
          a.href = BEZ + '#' + encodeURIComponent(p.id);
          li.appendChild(a);
          li.appendChild(document.createTextNode(' — ' + firstSentence(p.description)));
          list.appendChild(li);
        });
        var fallback = box.querySelector('.bez-apps-fallback');
        if (fallback) fallback.hidden = true;
        box.appendChild(list);
      });
    })
    .catch(function(){ /* keep the plain link */ });
})();
