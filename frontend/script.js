'use strict';
const tabs = [$('#condition-tab'), $('#ai-tab')];
// 브라우저 기본 선택창 대신 화면 디자인과 일치하는 아래쪽 메뉴를 사용합니다.
const dropdowns = [];
function createDropdown(name, choices) {
  const wrapper = document.querySelector(`[data-dropdown="${name}"]`);
  const trigger = wrapper.querySelector('.dropdown-trigger');
  const menu = wrapper.querySelector('.dropdown-menu');
  const list = wrapper.querySelector('[role="listbox"]');
  const input = document.getElementById(name);
  const valueLabel = document.getElementById(`${name}-value`);
  let active = Math.max(0, choices.findIndex(choice => choice.value === input.value));
  const options = choices.map((choice, index) => {
    const option = document.createElement('li');
    option.id = `${name}-option-${index}`;
    option.setAttribute('role', 'option');
    option.setAttribute('aria-selected', String(choice.value === input.value));
    if (choice.disabled) option.setAttribute('aria-disabled', 'true');
    option.textContent = choice.label;
    // 포인터 선택 시 combobox의 키보드 포커스를 유지합니다.
    option.addEventListener('pointerdown', event => event.preventDefault());
    option.addEventListener('click', () => choose(index));
    list.append(option);
    return option;
  });
  function highlight(index) {
    active = Math.max(0, Math.min(options.length - 1, index));
    options.forEach((option, i) => option.classList.toggle('is-active', i === active));
    trigger.setAttribute('aria-activedescendant', options[active].id);
    // 페이지 전체 대신 목록 안에서만 스크롤합니다.
    const option = options[active];
    if (option.offsetTop < list.scrollTop) list.scrollTop = option.offsetTop;
    else if (option.offsetTop + option.offsetHeight > list.scrollTop + list.clientHeight) {
      list.scrollTop = option.offsetTop + option.offsetHeight - list.clientHeight;
    }
  }
  function close() {
    menu.hidden = true;
    trigger.setAttribute('aria-expanded', 'false');
    trigger.removeAttribute('aria-activedescendant');
    wrapper.classList.remove('is-open');
  }
  function open() {
    dropdowns.forEach(dropdown => dropdown.close());
    menu.hidden = false;
    wrapper.classList.add('is-open');
    trigger.setAttribute('aria-expanded', 'true');
    highlight(choices.findIndex(choice => choice.value === input.value));
  }
  function choose(index) {
    // 준비 중인 항목은 선택되지 않고 안내만 보여 줍니다.
    if (choices[index].disabled) {
      showNotice($('#toast'), choices[index].notice);
      return;
    }
    input.value = choices[index].value;
    valueLabel.textContent = choices[index].label;
    options.forEach((option, i) => option.setAttribute('aria-selected', String(i === index)));
    input.dispatchEvent(new Event('change', { bubbles: true }));
    close();
    trigger.focus({ preventScroll: true });
  }
  trigger.addEventListener('click', () => menu.hidden ? open() : close());
  trigger.addEventListener('keydown', event => {
    if (event.key === 'Escape') { event.preventDefault(); close(); return; }
    if (event.key === 'Tab') { close(); return; }
    if (['ArrowDown', 'ArrowUp', 'Home', 'End'].includes(event.key)) {
      event.preventDefault();
      if (menu.hidden) {
        open();
        if (event.key === 'Home') highlight(0);
        if (event.key === 'End') highlight(options.length - 1);
      } else highlight(event.key === 'Home' ? 0 : event.key === 'End' ? options.length - 1 : active + (event.key === 'ArrowDown' ? 1 : -1));
    }
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      if (menu.hidden) open(); else choose(active);
    }
  });
  wrapper.addEventListener('focusout', event => { if (!wrapper.contains(event.relatedTarget)) close(); });
  dropdowns.push({ wrapper, close });
}

const DISTRICTS = ['마포구', '서대문구', '용산구', '영등포구'];
// 실제로 조사한 식당 데이터가 있는 지역. 나머지는 "준비 중"으로 보여 줍니다.
const OPEN_DISTRICTS = ['마포구'];
const MAX_PEOPLE = 50;
const initial = new URLSearchParams(location.search);
if (OPEN_DISTRICTS.includes(initial.get('district'))) {
  $('#district').value = initial.get('district');
  $('#district-value').textContent = initial.get('district');
}
const initialPeople = Number(initial.get('people'));
if (Number.isInteger(initialPeople) && initialPeople >= 1 && initialPeople <= MAX_PEOPLE) {
  $('#people').value = String(initialPeople);
  $('#people-value').textContent = initialPeople + '명';
}
if (initial.has('room')) $('#room').checked = initial.get('room') === 'true';
if (initial.has('query')) $('#query').value = initial.get('query').slice(0, 200);

// 시·도 → 구 2단계 선택. 식당 데이터가 있는 구(OPEN_DISTRICTS)만 고를 수 있고 나머지는 "준비 중"으로 표시합니다.
createRegionDropdown(DISTRICTS, OPEN_DISTRICTS);
createDropdown('people', Array.from({ length: MAX_PEOPLE }, (_, i) => ({ value: String(i + 1), label: `${i + 1}명` })));
document.addEventListener('pointerdown', event => {
  dropdowns.forEach(dropdown => { if (!dropdown.wrapper.contains(event.target)) dropdown.close(); });
});

function activateTab(index, focus = false) {
  dropdowns.forEach(dropdown => dropdown.close());
  tabs.forEach((tab, i) => {
    tab.setAttribute('aria-selected', String(i === index));
    tab.tabIndex = i === index ? 0 : -1;
    document.getElementById(tab.getAttribute('aria-controls')).hidden = i !== index;
  });
  if (focus) tabs[index].focus();
}
tabs.forEach((tab, i) => {
  tab.addEventListener('click', () => activateTab(i));
  tab.addEventListener('keydown', (event) => {
    if (['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) {
      event.preventDefault();
      activateTab(event.key === 'Home' ? 0 : event.key === 'End' ? 1 : 1 - i, true);
    }
  });
});

if (initial.get('mode') === 'ai') activateTab(1);
function startSearch(isAi) {
  const params = new URLSearchParams();
  if (isAi) {
    const query = $('#query').value.trim();
    if (!query) { showSearchNotice('검색 문장을 입력해 주세요.'); $('#query').focus(); return; }
    params.set('query', query);
  } else {
    params.set('district', $('#district').value);
    params.set('people', $('#people').value);
    params.set('room', String($('#room').checked));
  }
  location.href = 'results.html?' + params;
}
$('#condition-form').addEventListener('submit', event => { event.preventDefault(); startSearch(false); });
$('#ai-form').addEventListener('submit', event => { event.preventDefault(); startSearch(true); });
