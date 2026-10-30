'use strict';
const password = document.getElementById('password');
const toggle = document.getElementById('toggle-password');
const params = new URLSearchParams(location.search);
const loginRequired = document.getElementById('login-required');
const isFavorites = params.get('next') === 'favorites.html';
loginRequired.hidden = !isFavorites && params.get('next') !== 'mypage.html';
loginRequired.textContent = isFavorites
  ? '단골 목록은 로그인 후 사용할 수 있어요. 로그인하면 마음에 드는 식당을 모아볼 수 있어요.'
  : '마이페이지를 이용하려면 로그인이 필요해요.';
toggle.addEventListener('click', () => {
  const visible = password.type === 'password';
  password.type = visible ? 'text' : 'password';
  toggle.setAttribute('aria-pressed', String(visible));
  toggle.setAttribute('aria-label', visible ? '비밀번호 숨기기' : '비밀번호 보기');
  document.getElementById('eye-slash').style.display = visible ? 'block' : 'none';
});
let noticeTimer, hideTimer;
document.querySelectorAll('[data-login-notice]').forEach(button => button.addEventListener('click', () => {
  clearTimeout(noticeTimer); clearTimeout(hideTimer);
  const toast = document.getElementById('toast');
  toast.classList.remove('notice-fading');
  toast.textContent = button.dataset.loginNotice;
  toast.hidden = false;
  noticeTimer = setTimeout(() => {
    toast.classList.add('notice-fading');
    hideTimer = setTimeout(() => { toast.hidden = true; }, matchMedia('(prefers-reduced-motion: reduce)').matches ? 0 : 600);
  }, 4000);
}));
document.getElementById('login-form').addEventListener('submit', event => {
  event.preventDefault();
  // 인증 API가 준비되기 전에는 비밀번호를 전송·저장하거나 로그인 성공을 가장하지 않습니다.
  const feedback = document.getElementById('login-feedback');
  feedback.hidden = false;
  feedback.textContent = '로그인 서버 연결을 준비 중이에요. 지금은 로그인 없이 식당을 둘러볼 수 있어요.';
});
document.getElementById('login-submit').disabled = false;
