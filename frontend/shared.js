'use strict';
const API_BASE = 'http://localhost:8080';
const $ = selector => document.querySelector(selector);
const status = $('#status');
const noticeTimers = new WeakMap();
function showNotice(element, message) {
  const previous = noticeTimers.get(element);
  if (previous) previous.forEach(clearTimeout);
  element.classList.remove('notice-fading');
  element.textContent = message;
  element.hidden = !message;
  if (!message) { noticeTimers.delete(element); return; }
  const timers = [];
  noticeTimers.set(element, timers);
  timers.push(setTimeout(() => {
    element.classList.add('notice-fading');
    const duration = window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 0 : 600;
    timers.push(setTimeout(() => {
      element.hidden = true;
      element.textContent = '';
      element.classList.remove('notice-fading');
      noticeTimers.delete(element);
    }, duration));
  }, 4000));
}
function showSearchNotice(message) {
  showNotice(status, message);
}

async function request(path, options = {}) {
  const response = await fetch(API_BASE + path, options);
  const data = await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.message || `요청을 처리하지 못했어요. (${response.status})`);
  if (data === null) throw new Error('서버 응답을 확인할 수 없어요. 다시 시도해 주세요.');
  return data;
}
function node(tag, text, className) {
  const el = document.createElement(tag);
  if (text !== undefined) el.textContent = text;
  if (className) el.className = className;
  return el;
}
function photo(url, alt) {
  if (!url || !url.startsWith('/images/')) return node('div', '사진 준비 중', 'image-placeholder');
  const image = document.createElement('img');
  image.src = API_BASE + url;
  image.alt = alt;
  image.loading = 'lazy';
  image.addEventListener('error', () => image.replaceWith(node('div', '사진을 불러오지 못했어요', 'image-placeholder')), { once: true });
  return image;
}
document.querySelectorAll('[data-notice]').forEach((button) => {
  button.addEventListener('click', () => {
    showNotice($('#toast'), button.dataset.notice);
  });
});

const dialog = $('#detail-dialog');
let detailRequest;
$('#close-detail').addEventListener('click', () => dialog.close());
dialog.addEventListener('close', () => detailRequest?.abort());
async function showDetail(id) {
  detailRequest?.abort();
  const controller = new AbortController();
  detailRequest = controller;
  const timer = setTimeout(() => controller.abort(), 20000);
  const content = $('#detail-content');
  const heading = node('h2', '좌석 정보를 불러오고 있어요…');
  heading.id = 'detail-title';
  content.replaceChildren(heading);
  dialog.showModal();
  try {
    const data = await request(`/api/restaurants/${encodeURIComponent(id)}`, { signal: controller.signal });
    heading.textContent = data.name;
    content.append(node('p', data.address), node('p', `영업시간: ${data.businessHours || '정보 없음'}`), node('p', `전화: ${data.phone || '정보 없음'}`), node('p', `정보 확인일: ${data.lastVerifiedAt || '정보 없음'}`));
    content.append(node('h3', '좌석 구성'));
    const types = { TABLE: '일반 테이블', CONNECTED: '연결 단체석', ROOM: '룸' };
    if (!data.seatings.length) content.append(node('p', '등록된 좌석 정보가 없어요.'));
    data.seatings.forEach(seating => {
      const section = node('article');
      section.append(node('h4', `${seating.name} · ${types[seating.seatingType] || seating.seatingType}`), node('p', `${seating.minCapacity}~${seating.maxCapacity}명 · ${seating.quantity}개`));
      if (seating.description) section.append(node('p', seating.description));
      content.append(section);
    });
    const gallery = node('div', undefined, 'gallery');
    data.images.forEach(image => {
      const figure = node('figure');
      figure.append(photo(image.imageUrl, image.caption || data.name), node('figcaption', image.caption || '식당 사진'));
      gallery.append(figure);
    });
    content.append(gallery, node('p', '좌석 구성 안내이며 실시간 빈자리나 예약 가능 여부를 뜻하지 않습니다.'));
  } catch (error) {
    if (dialog.open) heading.textContent = error.name === 'AbortError' ? '응답이 지연되고 있어요. 닫은 뒤 다시 시도해 주세요.' : '상세 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.';
  } finally { clearTimeout(timer); }
}
