// =========================================
// TSunWaEnhancer — Minimal B&W Website UI
// Interactivity & animations
// =========================================

(function () {
    'use strict';

    // --- Day picker selection ---
    const dayItems = document.querySelectorAll('.day-item');
    dayItems.forEach(function (item) {
        item.addEventListener('click', function () {
            dayItems.forEach(function (i) {
                i.classList.remove('active');
                i.setAttribute('aria-selected', 'false');
            });
            item.classList.add('active');
            item.setAttribute('aria-selected', 'true');
        });
    });

    // --- Task card toggle (done / pending) ---
    const taskCards = document.querySelectorAll('.task-card');
    taskCards.forEach(function (card) {
        card.addEventListener('click', function () {
            card.classList.toggle('done');
            const check = card.querySelector('.check');
            if (card.classList.contains('done')) {
                check.classList.remove('check-off');
                check.classList.add('check-on');
                check.textContent = '\u2713';
            } else {
                check.classList.remove('check-on');
                check.classList.add('check-off');
                check.textContent = '\u25A1';
            }
        });
    });

    // --- Circular progress animation ---
    const progressCircle = document.querySelector('.circle-progress');
    const radius = 68;
    const circumference = 2 * Math.PI * radius; // ~427
    const percent = 20; // 20% completed
    const offset = circumference - (percent / 100) * circumference;

    if (progressCircle) {
        progressCircle.style.strokeDasharray = circumference;
        progressCircle.style.strokeDashoffset = circumference;
        // Animate on load
        requestAnimationFrame(function () {
            progressCircle.style.strokeDashoffset = offset;
        });
    }

    // --- Add button ripple ---
    const addBtn = document.querySelector('.add-btn');
    if (addBtn) {
        addBtn.addEventListener('click', function (e) {
            const ripple = document.createElement('span');
            const rect = addBtn.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;

            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            ripple.classList.add('ripple');
            addBtn.appendChild(ripple);

            setTimeout(function () {
                ripple.remove();
            }, 400);
        });
    }

    // --- Inject ripple style dynamically ---
    const style = document.createElement('style');
    style.textContent = `
        .add-btn {
            position: relative;
            overflow: hidden;
        }
        .ripple {
            position: absolute;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.2);
            transform: scale(0);
            animation: ripple 0.4s linear;
            pointer-events: none;
        }
        @keyframes ripple {
            to {
                transform: scale(4);
                opacity: 0;
            }
        }
    `;
    document.head.appendChild(style);

    console.log('TSunWaEnhancer UI loaded.');
})();