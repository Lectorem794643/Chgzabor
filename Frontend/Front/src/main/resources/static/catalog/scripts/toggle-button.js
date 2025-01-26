document.querySelectorAll('.toggle-btn').forEach(button => {
    button.addEventListener('click', () => {
        const subList = button.nextElementSibling;
        if (subList && subList.classList.contains('sub-list')) {
            subList.classList.toggle('open');
        }
    });
});
