'use strict';
const params = new URLSearchParams(location.search);
let conditions = { district: params.get('district') || '마포구', people: params.has('people') ? Number(params.get('people')) : null, room: params.get('room') === 'true' };
const validTypes = ['ROOM', 'TABLE', 'CONNECTED'];
let appliedTypes = params.getAll('type').filter(type => validTypes.includes(type));
if (!appliedTypes.length && conditions.room) appliedTypes = ['ROOM'];
let restaurants = [];
let sort = params.get('sort') === 'capacity' ? 'capacity' : 'default';
let aiQuery = params.get('query');
let loading = false;
let controller;
const names = { ROOM: '룸', TABLE: '일반 테이블', CONNECTED: '연결 단체석' };
const list = $('#restaurant-list');
// 로그인한 사용자의 단골 식당 id (로그인 안 했으면 비어 있음)
let favoriteIds = new Set();

function checkedTypes() { return [...document.querySelectorAll('input[name="seating"]:checked')].map(input => input.value); }
function syncCheckboxes() { document.querySelectorAll('input[name="seating"]').forEach(input => { input.checked = appliedTypes.includes(input.value); }); }
function matchesParty(seating) { return conditions.people === null || (seating.minCapacity <= conditions.people && conditions.people <= seating.maxCapacity); }
function matchesFilter(restaurant) {
  if (!appliedTypes.length) return true;
  // 여러 항목을 선택하면 그중 하나에 해당하는 좌석에 전원이 앉을 수 있어야 합니다.
  return restaurant.detail.seatings.some(seating => appliedTypes.includes(seating.seatingType) && matchesParty(seating));
}
function writeLocation() {
  const next = new URLSearchParams();
  if (aiQuery) next.set('query', aiQuery);
  else {
    next.set('district', conditions.district || '');
    if (conditions.people !== null) next.set('people', conditions.people);
    next.set('room', String(appliedTypes.length === 1 && appliedTypes[0] === 'ROOM'));
    appliedTypes.forEach(type => next.append('type', type));
  }
  if (sort !== 'default') next.set('sort', sort);
  history.replaceState(null, '', 'results.html?' + next);
}
function updateChips() {
  const chips = $('#condition-chips');
  chips.replaceChildren();
  [conditions.district || '전체 지역', conditions.people === null ? '인원 상관없음' : `${conditions.people}명`, ...(appliedTypes.length ? appliedTypes.map(type => names[type]) : ['자리 형태 전체'])].forEach(text => chips.append(node('span', text, 'condition-chip')));
  const edit = new URLSearchParams();
  edit.set('district', conditions.district || '마포구');
  if (conditions.people !== null) edit.set('people', conditions.people);
  edit.set('room', String(appliedTypes.includes('ROOM')));
  if (aiQuery) { edit.set('mode', 'ai'); edit.set('query', aiQuery); }
  $('#edit-conditions').href = 'index.html?' + edit;
}
function render() {
  const shown = restaurants.filter(matchesFilter);
  if (sort === 'capacity') shown.sort((a, b) => (b.maxGroupCapacity ?? -1) - (a.maxGroupCapacity ?? -1));
  list.replaceChildren();
  const party = conditions.people === null ? '함께' : `${conditions.people}명이 함께`;
  $('#result-title').textContent = shown.length ? `${party} 앉을 수 있는 자리 ${shown.length}곳을 찾았어요` : '조건에 맞는 자리를 찾지 못했어요';
  $('#result-count').textContent = `총 ${shown.length}곳`;
  // AI 안내 문장이 이미 보이면 같은 뜻의 안내를 한 번 더 보여 주지 않습니다.
  $('#result-state').hidden = shown.length > 0 || !$('#ai-explanation').hidden;
  $('#result-state').textContent = shown.length ? '' : '조건에 맞는 식당이 없어요. 필터를 초기화하거나 검색 조건을 바꿔보세요.';
  shown.forEach(restaurant => {
    const row = node('article', undefined, 'restaurant-row');
    const photoButton = node('button', undefined, 'restaurant-photo');
    photoButton.type = 'button';
    photoButton.setAttribute('aria-label', `${restaurant.name} 좌석 상세 보기`);
    photoButton.append(photo(restaurant.mainImageUrl, `${restaurant.name} 좌석 사진`));
    photoButton.addEventListener('click', () => showDetail(restaurant.id));
    const info = node('div', undefined, 'restaurant-info');
    const top = node('div', undefined, 'restaurant-top');
    const save = node('button', undefined, 'save-button');
    save.type = 'button';
    // ♥: 단골에 있음, ♡: 없음. 누를 때마다 추가·삭제하고, 로그인 안 했으면 로그인 화면으로 갑니다
    const showSaved = saved => {
      save.textContent = saved ? '♥' : '♡';
      save.setAttribute('aria-pressed', String(saved));
      save.setAttribute('aria-label', `${restaurant.name} ${saved ? '단골에서 빼기' : '단골 목록에 추가'}`);
      save.title = saved ? '단골에서 빼기' : '단골 목록에 추가';
    };
    showSaved(favoriteIds.has(restaurant.id));
    save.addEventListener('click', async () => {
      const next = !favoriteIds.has(restaurant.id);
      save.disabled = true;
      if (await setFavorite(restaurant.id, next)) {
        if (next) favoriteIds.add(restaurant.id); else favoriteIds.delete(restaurant.id);
        showSaved(next);
        showNotice($('#toast'), next ? `${restaurant.name}을(를) 단골에 추가했어요.` : `${restaurant.name}을(를) 단골에서 뺐어요.`);
      }
      save.disabled = false;
    });
    top.append(node('h2', restaurant.name), save);
    info.append(top, node('p', `${restaurant.district} · ${restaurant.category || '식당'}`, 'restaurant-meta'), node('p', restaurant.address, 'restaurant-address'));
    const tags = node('div', undefined, 'restaurant-tags');
    const available = [...new Set(restaurant.detail.seatings.filter(matchesParty).map(seating => seating.seatingType))];
    available.forEach(type => tags.append(node('span', names[type] || type)));
    tags.append(node('span', restaurant.maxGroupCapacity === null ? '좌석 정보 없음' : `함께 최대 ${restaurant.maxGroupCapacity}명`));
    info.append(tags);
    const bottom = node('div', undefined, 'restaurant-bottom');
    const detailButton = node('button', '자리 확인하기', 'detail-link');
    detailButton.type = 'button';
    detailButton.addEventListener('click', () => showDetail(restaurant.id));
    bottom.append(node('p', restaurant.detail.lastVerifiedAt ? `정보 확인일 ${restaurant.detail.lastVerifiedAt}` : '실제 좌석 정보를 확인해 보세요', ''), detailButton);
    info.append(bottom);
    row.append(photoButton, info);
    list.append(row);
  });
  updateChips();
  document.querySelectorAll('[data-sort]').forEach(button => button.setAttribute('aria-pressed', String(button.dataset.sort === sort)));
}
// AI 검색은 하루 사용 한도가 있어서, 같은 문장의 결과는 이 탭에 저장해 두고 새로고침할 때 다시 씁니다.
// AI 설명을 받지 못한 결과(message가 null)는 저장하지 않아 다음에 다시 시도합니다.
async function searchWithAi(query, signal) {
  const key = 'ai-search:' + query;
  try {
    const saved = sessionStorage.getItem(key);
    if (saved) return JSON.parse(saved);
  } catch { /* 저장소를 쓸 수 없으면 서버에 요청합니다. */ }
  const data = await request('/api/ai/search', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ query }), signal });
  if (data.message) {
    try { sessionStorage.setItem(key, JSON.stringify(data)); } catch { /* 저장하지 못해도 결과는 그대로 보여 줍니다. */ }
  }
  return data;
}
async function load() {
  controller?.abort();
  controller = new AbortController();
  const signal = controller.signal;
  const timeout = setTimeout(() => controller.abort(), 45000);
  loading = true;
  list.replaceChildren();
  list.setAttribute('aria-busy', 'true');
  $('#result-title').textContent = '함께 앉을 수 있는 자리를 찾고 있어요';
  $('#result-count').textContent = '';
  $('#result-state').hidden = false;
  $('#result-state').textContent = aiQuery ? 'AI가 모임 조건과 실제 좌석 정보를 확인하고 있어요.' : '식당과 좌석 정보를 확인하고 있어요.';
  $('#retry').hidden = true;
  document.querySelectorAll('#filter-form button').forEach(button => { button.disabled = true; });
  try {
    if (conditions.people !== null && (!Number.isInteger(conditions.people) || conditions.people < 1)) throw Object.assign(new Error('인원은 1명 이상의 정수로 입력해 주세요.'), { inputError: true });
    let summaries;
    if (aiQuery) {
      const data = await searchWithAi(aiQuery, signal);
      conditions = { ...data.conditions, people: data.conditions.people ?? null };
      appliedTypes = conditions.room ? ['ROOM'] : [];
      summaries = data.restaurants;
      $('#ai-explanation').textContent = data.message || '';
      $('#ai-explanation').hidden = !data.message;
    } else {
      const searchParams = new URLSearchParams({ district: conditions.district || '', room: 'false' });
      if (conditions.people !== null) searchParams.set('people', conditions.people);
      summaries = await request('/api/restaurants?' + searchParams, { signal });
      $('#ai-explanation').hidden = true;
    }
    // 현재 조사된 소규모 목록의 상세 좌석을 확인해 인원과 좌석 종류를 함께 검증합니다.
    [restaurants, favoriteIds] = await Promise.all([
      Promise.all(summaries.map(async restaurant => ({ ...restaurant, detail: await request(`/api/restaurants/${encodeURIComponent(restaurant.id)}`, { signal }) }))),
      loadFavoriteIds()
    ]);
    syncCheckboxes();
    render();
    writeLocation();
    showSearchNotice('');
  } catch (error) {
    controller.abort();
    restaurants = [];
    // 인원처럼 입력이 잘못된 경우(400 등)는 다시 시도해도 같으므로, 연결 안내 대신 조건을 바꾸도록 안내합니다.
    const inputError = error.inputError || (error.status >= 400 && error.status < 500);
    if (inputError) {
      $('#result-title').textContent = '검색 조건을 확인해 주세요';
      $('#result-state').textContent = `${error.message} 조건 바꾸기를 눌러 다시 검색해 주세요.`;
      $('#retry').hidden = true;
      return;
    }
    $('#result-title').textContent = '검색 결과를 불러오지 못했어요';
    $('#result-state').textContent = '연결 상태를 확인한 뒤 다시 시도해 주세요.';
    $('#retry').hidden = false;
    showSearchNotice(error.name === 'AbortError' ? '응답이 지연되고 있어요. 다시 시도해 주세요.' : error instanceof TypeError ? '서버에 연결할 수 없어요. 백엔드를 실행한 뒤 다시 시도해 주세요.' : error.message);
  } finally {
    clearTimeout(timeout);
    loading = false;
    list.setAttribute('aria-busy', 'false');
    document.querySelectorAll('#filter-form button').forEach(button => { button.disabled = false; });
  }
}
$('#filter-form').addEventListener('submit', event => {
  event.preventDefault();
  if (loading) return;
  appliedTypes = checkedTypes();
  aiQuery = null;
  writeLocation();
  load();
});
$('#reset-filter').addEventListener('click', () => {
  if (loading) return;
  appliedTypes = [];
  aiQuery = null;
  syncCheckboxes();
  writeLocation();
  load();
});
document.querySelectorAll('[data-sort]').forEach(button => button.addEventListener('click', () => {
  sort = button.dataset.sort;
  document.querySelectorAll('[data-sort]').forEach(item => item.setAttribute('aria-pressed', String(item === button)));
  if (!loading && restaurants.length) render();
  writeLocation();
}));
$('#retry').addEventListener('click', load);
syncCheckboxes();
updateChips();
load();
