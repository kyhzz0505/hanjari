'use strict';
// 로그인한 사용자의 단골 목록을 보여 줍니다. 로그인하지 않았으면 로그인 화면으로 보냅니다.
let favorites = [];
let favoritesLoading = false;
let favoriteLoadError = false;
function seatText(restaurant) {
  return `${restaurant.hasRoom ? '룸 보유 · ' : ''}${restaurant.maxGroupCapacity == null ? '좌석 정보 없음' : '최대 ' + restaurant.maxGroupCapacity + '명'}`;
}
// ♥를 누르면 단골에서 빼고 목록을 다시 그립니다
function heartButton(restaurant, className) {
  const heart = node('button', '♥', className);
  heart.type = 'button';
  heart.title = '단골에서 빼기';
  heart.setAttribute('aria-label', `${restaurant.name} 단골에서 빼기`);
  heart.addEventListener('click', async () => {
    heart.disabled = true;
    if (!(await setFavorite(restaurant.id, false))) { heart.disabled = false; return; }
    favorites = favorites.filter(item => item.id !== restaurant.id);
    renderFavorites();
    showNotice($('#toast'), `${restaurant.name}을(를) 단골에서 뺐어요.`);
  });
  return heart;
}
function renderPreview() {
  const grid = $('#favorite-preview');
  const state = $('#favorite-preview-state');
  grid.replaceChildren();
  grid.setAttribute('aria-busy', String(favoritesLoading));
  $('#favorites-total').textContent = favoritesLoading || favoriteLoadError ? '—' : favorites.length;
  $('#preview-retry').hidden = !favoriteLoadError;
  state.hidden = !favoritesLoading && !favoriteLoadError && favorites.length > 0;
  state.textContent = favoritesLoading ? '단골 목록을 불러오고 있어요.' : favoriteLoadError ? '단골 목록을 불러오지 못했어요. 서버 연결을 확인해 주세요.' : favorites.length ? '' : '아직 단골이 없어요. 검색 결과에서 ♡를 눌러 보세요.';
  favorites.slice(0, 3).forEach(restaurant => {
    const card = node('article', undefined, 'favorite-card');
    const open = node('button', undefined, 'favorite-open');
    open.type = 'button';
    open.setAttribute('aria-label', `${restaurant.name} 상세 보기`);
    const body = node('div', undefined, 'favorite-body');
    body.append(node('h3', restaurant.name), node('p', `${restaurant.district} · ${restaurant.category || '식당'}`), node('span', seatText(restaurant), 'tag'));
    open.append(photo(restaurant.mainImageUrl, `${restaurant.name} 실제 사진`), body);
    open.addEventListener('click', () => showDetail(restaurant.id));
    card.append(open, heartButton(restaurant, 'favorite-heart'));
    grid.append(card);
  });
}
function renderFavoritesModal() {
  const list = $('#favorites-list');
  list.replaceChildren();
  $('#favorites-count').textContent = favorites.length;
  $('#favorites-retry').hidden = !favoriteLoadError;
  $('#favorites-modal-state').textContent = favoritesLoading ? '단골 목록을 불러오고 있어요.' : favoriteLoadError ? '목록을 불러오지 못했어요. 다시 시도해 주세요.' : favorites.length ? '' : '아직 단골이 없어요. 검색 결과에서 ♡를 눌러 보세요.';
  list.setAttribute('aria-busy', String(favoritesLoading));
  // 서버가 최근에 추가한 순서로 보내 주므로 기본 정렬은 그대로 씁니다
  const items = [...favorites];
  if ($('#favorites-sort').value === 'name') items.sort((a, b) => a.name.localeCompare(b.name, 'ko'));
  if ($('#favorites-sort').value === 'capacity') items.sort((a, b) => (b.maxGroupCapacity || 0) - (a.maxGroupCapacity || 0));
  items.forEach(restaurant => {
    const row = node('article', undefined, 'favorites-row');
    const open = node('button', undefined, 'favorites-row-open');
    open.type = 'button';
    open.setAttribute('aria-label', `${restaurant.name} 상세 보기`);
    const body = node('div', undefined, 'favorites-row-body');
    body.append(node('h3', restaurant.name), node('p', `${restaurant.district} · ${restaurant.category || '식당'}`), node('span', seatText(restaurant), 'tag'));
    const arrow = node('span', '›', 'favorites-row-arrow');
    arrow.setAttribute('aria-hidden', 'true');
    open.append(photo(restaurant.mainImageUrl, `${restaurant.name} 실제 사진`), body, arrow);
    open.addEventListener('click', () => showDetail(restaurant.id));
    row.append(open, heartButton(restaurant, 'favorites-row-heart'));
    list.append(row);
  });
}
function renderFavorites() { renderPreview(); renderFavoritesModal(); }
async function loadFavorites() {
  if (favoritesLoading) return;
  favoritesLoading = true;
  favoriteLoadError = false;
  renderFavorites();
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 15000);
  try {
    favorites = await request('/api/favorites', { signal: controller.signal });
  } catch (error) {
    // 그사이 로그아웃됐으면 로그인 화면으로 보냅니다
    if (error.status === 401) { location.replace(loginUrl()); return; }
    favorites = [];
    favoriteLoadError = true;
  } finally { clearTimeout(timeout); favoritesLoading = false; renderFavorites(); }
}
const favoritesModal = $('#favorites-dialog');
let previousBodyOverflow;
$('#open-favorites').addEventListener('click', () => {
  renderFavoritesModal();
  previousBodyOverflow = document.body.style.overflow;
  document.body.style.overflow = 'hidden';
  favoritesModal.showModal();
  $('.favorites-scroll').scrollTop = 0;
});
$('#close-favorites').addEventListener('click', () => favoritesModal.close());
favoritesModal.addEventListener('close', () => {
  document.body.style.overflow = previousBodyOverflow;
  $('#open-favorites').focus();
});
favoritesModal.addEventListener('click', event => {
  const rect = favoritesModal.getBoundingClientRect();
  if (event.target === favoritesModal && (event.clientX < rect.left || event.clientX > rect.right || event.clientY < rect.top || event.clientY > rect.bottom)) favoritesModal.close();
});
$('#favorites-sort').addEventListener('change', renderFavoritesModal);
$('#favorites-retry').addEventListener('click', loadFavorites);
$('#preview-retry').addEventListener('click', loadFavorites);
$('#mypage-logout').addEventListener('click', logout);
const sidebarLinks = document.querySelectorAll('.mypage-sidebar nav a');
function markActive(link) { sidebarLinks.forEach(item => item.classList.toggle('active', item === link)); }
sidebarLinks.forEach(link => link.addEventListener('click', () => markActive(link)));
// mypage.html#favorites 처럼 들어오면 왼쪽 메뉴도 그 항목을 표시합니다
const linkForHash = [...sidebarLinks].find(link => link.getAttribute('href') === location.hash);
if (linkForHash) markActive(linkForHash);
getCurrentUser().then(user => {
  if (!user) { location.replace(loginUrl()); return; }
  $('#profile-name').textContent = `${user.displayName}님`;
  $('#overview').setAttribute('aria-busy', 'false');
  loadFavorites();
});
