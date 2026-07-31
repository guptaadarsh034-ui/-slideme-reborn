function openSidebar() {
  document.getElementById("sidebar").style.width = "250px";
}

function loadSource(url) {
  document.getElementById("webframe").style.display = "block";
  document.getElementById("webframe").src = url;
  document.getElementById("sidebar").style.width = "0"; // close menu
}

// Swipe to open
let x1 = null;
document.addEventListener('touchstart', e => x1 = e.touches[0].clientX);
document.addEventListener('touchmove', e => {
  if (!x1) return;
  let x2 = e.touches[0].clientX;
  if (x2 - x1 > 50) openSidebar(); // swipe right
  x1 = null;
});