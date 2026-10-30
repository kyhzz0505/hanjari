'use strict';
// 메인 하단에서도 실제 등록된 식당만 표시합니다.
let featuredLoading = false;
async function loadFeatured() {
  if (featuredLoading) return;
  featuredLoading = true;
  const state = $('#featured-state');
  const grid = $('#featured-grid');
  const retry = $('#featured-retry');
  grid.setAttribute('aria-busy', 'true');
  state.hidden = false;
  state.textContent = '등록된 식당을 불러오고 있어요.';
  retry.hidden = true;
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 15000);
  try {
    const data = await request('/api/restaurants?district=' + encodeURIComponent('마포구'), { signal: controller.signal });
    grid.replaceChildren();
    data.slice(0, 3).forEach(restaurant => {
      const card = node('button', undefined, 'featured-card');
      card.type = 'button';
      card.setAttribute('aria-label', `${restaurant.name} 좌석 확인하기`);
      card.append(photo(restaurant.mainImageUrl, `${restaurant.name} 실제 사진`), node('h3', restaurant.name), node('p', `${restaurant.district} · ${restaurant.category || '식당'}`));
      const tags = node('div', undefined, 'featured-tags');
      if (restaurant.hasRoom) tags.append(node('span', '룸 보유'));
      tags.append(node('span', restaurant.maxGroupCapacity == null ? '좌석 정보 없음' : `함께 최대 ${restaurant.maxGroupCapacity}명`));
      card.append(tags);
      card.addEventListener('click', () => showDetail(restaurant.id));
      grid.append(card);
    });
    state.hidden = data.length > 0;
    state.textContent = data.length ? '' : '아직 등록된 식당이 없어요.';
  } catch {
    state.textContent = '식당 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.';
    retry.hidden = false;
  } finally {
    clearTimeout(timeout);
    featuredLoading = false;
    grid.setAttribute('aria-busy', 'false');
  }
}
$('#featured-retry').addEventListener('click', loadFeatured);
$('#service-intro').addEventListener('click', () => {
  const content = $('#detail-content');
  const title = node('h2', '우리 모임이 함께 앉을 곳, 한자리');
  title.id = 'detail-title';
  content.replaceChildren(title, node('p', '한자리는 단체 모임에 맞는 식당을 실제 좌석 정보와 사진으로 찾아주는 서비스입니다. 지역과 인원, 자리 형태를 비교하고 우리 모임에 맞는 공간을 확인해 보세요.', 'service-copy'), node('p', '현재 마포구 식당을 안내합니다. 좌석 구성 정보이며 실시간 빈자리 확인이나 예약 서비스는 제공하지 않습니다.', 'service-copy'));
  $('#detail-dialog').showModal();
});
loadFeatured();
