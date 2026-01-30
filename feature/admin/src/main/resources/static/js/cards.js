document.addEventListener('DOMContentLoaded', () => {
    const createModal = createSimpleModal('createCardModal', 'openCreateModalBtn');
    const editModal = createEditModal('editCardModal', 'editCardForm');
    const deleteModal = createDeleteModal('deleteConfirmModal', 'deleteCardForm');

    initAlertDismissal('.alert');

    const table = document.querySelector('.table');
    if (table) {
        table.addEventListener('click', (event) => {
            const target = event.target;
            const editBtn = target.closest('[data-action="edit"]');
            const deleteBtn = target.closest('[data-action="delete"]');

            if (editBtn) {
                const data = {
                    id: editBtn.dataset.id,
                    title: editBtn.dataset.title,
                    slug: editBtn.dataset.slug,
                    description: editBtn.dataset.description,
                    status: editBtn.dataset.status,
                    rarity: editBtn.dataset.rarity,
                    categoryId: editBtn.dataset.categoryId
                };
                editModal.open(data);
            }

            if (deleteBtn) {
                const id = deleteBtn.dataset.id;
                deleteModal.open(id);
            }
        });
    }
});

function initAlertDismissal(selector, duration = 5000) {
    const alert = document.querySelector(selector);
    if (!alert) return;

    setTimeout(() => {
        alert.style.transition = 'opacity 0.5s ease-out, transform 0.5s ease-out';
        alert.style.opacity = '0';
        alert.style.transform = 'translateY(-10px)';
        setTimeout(() => alert.remove(), 500);
    }, duration);
}

function createSimpleModal(modalId, openBtnId) {
    const modal = document.getElementById(modalId);
    const openBtn = document.getElementById(openBtnId);

    if (!modal) return { open: () => {} };

    const close = () => {
        modal.classList.remove('modal--active');
        const form = modal.querySelector('form');
        if (form) form.reset();
    };

    const open = () => modal.classList.add('modal--active');

    bindModalEvents(modal, close);

    if (openBtn) {
        openBtn.addEventListener('click', open);
    }

    return { open, close };
}

function createEditModal(modalId, formId) {
    const modal = document.getElementById(modalId);
    const form = document.getElementById(formId);

    if (!modal || !form) return { open: () => {} };

    const inputs = {
        title: form.querySelector('[name="title"]'),
        slug: form.querySelector('[name="slug"]'),
        description: form.querySelector('[name="description"]'),
        status: form.querySelector('[name="status"]'),
        rarity: form.querySelector('[name="rarity"]'),
        categoryId: form.querySelector('[name="categoryId"]')
    };

    const close = () => {
        modal.classList.remove('modal--active');
        form.reset();
    };

    const open = (data) => {
        form.action = `/admin/cards/${data.id}/update`;

        inputs.title.value = data.title || '';
        inputs.slug.value = data.slug || '';
        inputs.description.value = data.description || '';
        inputs.status.value = data.status || 'Hidden';
        inputs.rarity.value = data.rarity || 'COMMON';
        inputs.categoryId.value = data.categoryId || '';

        modal.classList.add('modal--active');
    };

    bindModalEvents(modal, close);

    return { open, close };
}

function createDeleteModal(modalId, formId) {
    const modal = document.getElementById(modalId);
    const form = document.getElementById(formId);

    if (!modal || !form) return { open: () => {} };

    const close = () => modal.classList.remove('modal--active');

    const open = (id) => {
        form.action = `/admin/cards/${id}/delete`;
        modal.classList.add('modal--active');
    };

    bindModalEvents(modal, close);

    return { open, close };
}

function bindModalEvents(modal, closeCallback) {
    modal.querySelectorAll('[data-modal-close]').forEach(btn => {
        btn.addEventListener('click', closeCallback);
    });

    modal.addEventListener('click', (event) => {
        if (event.target === modal) closeCallback();
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && modal.classList.contains('modal--active')) {
            closeCallback();
        }
    });
}