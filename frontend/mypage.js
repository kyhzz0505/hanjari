'use strict';
// 디자인 확인용 공개 예시입니다. 회원 정보나 단골 저장 내역을 읽거나 쓰지 않습니다.
let previewLoading = false;
let favoriteExamples = [];
let favoriteLoadError = false;
async function loadFavoritePreview() {
  if (previewLoading) return;
  previewLoading = true;
  favoriteLoadError = false;
  renderFavoritesModal();
  const grid = $('#favorite-preview');
  const state = $('#favorite-preview-state');
  grid.setAttribute('aria-busy', 'true');
  state.hidden = false;
  state.textContent = '식당 예시를 불러오고 있어요.';
  $('#preview-retry').hidden = true;
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 15000);
  try {
    const restaurants = await request('/api/restaurants', { signal: controller.signal });
    favoriteExamples = restaurants;
    grid.replaceChildren();
    restaurants.slice(0, 3).forEach(restaurant => {
      const card = node('article', undefined, 'favorite-card');
      const open = node('button', undefined, 'favorite-open');
      open.type = 'button';
      open.setAttribute('aria-label', `${restaurant.name} 상세 보기`);
      const body = node('div', undefined, 'favorite-body');
      body.append(node('h3', restaurant.name), node('p', `${restaurant.district} · ${restaurant.category || '식당'}`), node('span', `${restaurant.hasRoom ? '룸 보유 · ' : ''}${restaurant.maxGroupCapacity == null ? '좌석 정보 없음' : '최대 ' + restaurant.maxGroupCapacity + '명'}`, 'tag'));
      open.append(photo(restaurant.mainImageUrl, `${restaurant.name} 실제 사진`), body);
      open.addEventListener('click', () => showDetail(restaurant.id));
      const heart = node('button', '♡', 'favorite-heart');
      heart.type = 'button';
      heart.setAttribute('aria-label', `${restaurant.name} 단골 목록에 추가`);
      heart.addEventListener('click', () => showNotice($('#toast'), '로그인 후 단골목록을 사용하세요'));
      card.append(open, heart, node('span', '화면 예시', 'preview-image-label'));
      grid.append(card);
    });
    state.hidden = restaurants.length > 0;
    state.textContent = restaurants.length ? '' : '등록된 식당이 없어요.';
  } catch {
    favoriteLoadError = true;
    state.textContent = '식당 예시를 불러오지 못했어요. 서버 연결을 확인해 주세요.';
    $('#preview-retry').hidden = false;
  } finally { clearTimeout(timeout); grid.setAttribute('aria-busy', 'false'); previewLoading = false; renderFavoritesModal(); }
}
function renderFavoritesModal() {
  const list = $('#favorites-list');
  list.replaceChildren();
  $('#favorites-count').textContent = favoriteExamples.length;
  $('#favorites-retry').hidden = !favoriteLoadError;
  $('#favorites-modal-state').textContent = previewLoading ? '식당 목록을 불러오고 있어요.' : favoriteLoadError ? '목록을 불러오지 못했어요. 다시 시도해 주세요.' : favoriteExamples.length ? '' : '아직 표시할 식당이 없어요.';
  list.setAttribute('aria-busy', String(previewLoading));
  const items = [...favoriteExamples];
  if ($('#favorites-sort').value === 'name') items.sort((a, b) => a.name.localeCompare(b.name, 'ko'));
  if ($('#favorites-sort').value === 'capacity') items.sort((a, b) => (b.maxGroupCapacity || 0) - (a.maxGroupCapacity || 0));
  items.forEach(restaurant => {
    const row = node('article', undefined, 'favorites-row');
    const open = node('button', undefined, 'favorites-row-open');
    open.type = 'button';
    open.setAttribute('aria-label', `${restaurant.name} 상세 보기`);
    const body = node('div', undefined, 'favorites-row-body');
    body.append(node('h3', restaurant.name), node('p', `${restaurant.district} · ${restaurant.category || '식당'}`), node('span', `${restaurant.hasRoom ? '룸 보유 · ' : ''}${restaurant.maxGroupCapacity == null ? '좌석 정보 없음' : '최대 ' + restaurant.maxGroupCapacity + '명'}`, 'tag'));
    const arrow = node('span', '›', 'favorites-row-arrow');
    arrow.setAttribute('aria-hidden', 'true');
    open.append(photo(restaurant.mainImageUrl, `${restaurant.name} 실제 사진`), body, arrow);
    open.addEventListener('click', () => showDetail(restaurant.id));
    const heart = node('a', '♡', 'favorites-row-heart');
    heart.href = 'login.html?next=favorites.html';
    heart.setAttribute('aria-label', `${restaurant.name} 단골 저장을 위해 로그인`);
    row.append(open, heart);
    list.append(row);
  });
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
$('#favorites-retry').addEventListener('click', loadFavoritePreview);
$('#preview-retry').addEventListener('click', loadFavoritePreview);
document.querySelectorAll('.mypage-sidebar nav a').forEach(link => link.addEventListener('click', () => {
  document.querySelectorAll('.mypage-sidebar nav a').forEach(item => item.classList.toggle('active', item === link));
}));
loadFavoritePreview();
