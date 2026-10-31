'use strict';
// openDistricts: 실제 식당 데이터가 있어 선택할 수 있는 구. 없는 구는 "준비 중"으로 보여 줍니다.
function createRegionDropdown(seoulDistricts, openDistricts = seoulDistricts) {
  const wrapper = document.querySelector('[data-dropdown="district"]');
  const trigger = wrapper.querySelector('.dropdown-trigger');
  const menu = wrapper.querySelector('.dropdown-menu');
  const input = document.getElementById('district');
  const valueLabel = document.getElementById('district-value');
  const regions = { 서울: seoulDistricts, 인천: [], 경기: [] };
  trigger.setAttribute('role', 'button');
  trigger.setAttribute('aria-haspopup', 'dialog');
  trigger.setAttribute('aria-controls', 'region-panel');
  menu.id = 'region-panel';
  menu.setAttribute('role', 'dialog');
  menu.setAttribute('aria-label', '지역 선택');
  menu.classList.add('region-menu');
  menu.replaceChildren();
  const heading = document.createElement('p');
  heading.className = 'region-heading';
  heading.textContent = '시·도를 먼저 선택해 주세요';
  const cities = document.createElement('div');
  cities.className = 'region-cities';
  cities.setAttribute('role', 'group');
  cities.setAttribute('aria-label', '시·도');
  const caption = document.createElement('p');
  caption.className = 'dropdown-caption';
  caption.setAttribute('aria-live', 'polite');
  const districts = document.createElement('div');
  districts.className = 'region-districts';
  districts.setAttribute('role', 'group');
  districts.setAttribute('aria-label', '구 선택');
  menu.append(heading, cities, caption, districts);
  function showRegion(city) {
    cities.querySelectorAll('button').forEach(button => button.setAttribute('aria-pressed', String(button.textContent === city)));
    caption.textContent = regions[city].length ? `${city} · 구를 선택해 주세요` : `${city} 지역은 준비 중이에요. 현재 서울 지역을 선택할 수 있어요.`;
    districts.replaceChildren();
    regions[city].forEach(district => {
      const button = document.createElement('button');
      button.type = 'button';
      button.textContent = district;
      button.setAttribute('aria-pressed', String(input.value === district));
      if (!openDistricts.includes(district)) {
        button.textContent = `${district} (준비 중)`;
        button.setAttribute('aria-disabled', 'true');
        button.addEventListener('click', () => showNotice($('#toast'), `${district} 식당은 준비 중이에요. 지금은 마포구 식당을 안내해요.`));
        districts.append(button);
        return;
      }
      button.addEventListener('click', () => {
        input.value = district;
        valueLabel.textContent = district;
        trigger.setAttribute('aria-label', `지역 서울 ${district}`);
        input.dispatchEvent(new Event('change', { bubbles: true }));
        close();
        trigger.focus({ preventScroll: true });
      });
      districts.append(button);
    });
  }
  Object.keys(regions).forEach(city => {
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = city;
    button.setAttribute('aria-pressed', 'false');
    button.addEventListener('click', () => showRegion(city));
    cities.append(button);
  });
  function close() {
    menu.hidden = true;
    trigger.setAttribute('aria-expanded', 'false');
    wrapper.classList.remove('is-open');
  }
  function open() {
    dropdowns.forEach(dropdown => dropdown.close());
    showRegion('서울');
    menu.hidden = false;
    trigger.setAttribute('aria-expanded', 'true');
    wrapper.classList.add('is-open');
    cities.querySelector('button').focus({ preventScroll: true });
  }
  trigger.addEventListener('click', () => menu.hidden ? open() : close());
  trigger.addEventListener('keydown', event => { if (event.key === 'ArrowDown') { event.preventDefault(); open(); } });
  wrapper.addEventListener('keydown', event => {
    if (event.key === 'Escape') { event.preventDefault(); close(); trigger.focus({ preventScroll: true }); }
  });
  wrapper.addEventListener('focusout', event => { if (!wrapper.contains(event.relatedTarget)) close(); });
  dropdowns.push({ wrapper, close });
}
