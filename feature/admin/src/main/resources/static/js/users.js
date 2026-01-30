document.addEventListener('DOMContentLoaded', () => {
    const editModal = createEditModal('editUserModal', 'editUserForm');
    const deleteModal = createDeleteModal('deleteConfirmModal', 'confirmDeleteBtn');

    initAlertDismissal('.alert');

    const table = document.querySelector('.table');

    if (table) {
        table.addEventListener('click', async (event) => {
            const target = event.target;
            const editBtn = target.closest('[data-action="edit"]');
            const deleteBtn = target.closest('[data-action="delete"]');

            if (editBtn) {
                const userData = {
                    id: editBtn.dataset.id,
                    fullName: editBtn.dataset.fullname,
                    email: editBtn.dataset.email,
                    role: editBtn.dataset.role
                };
                editModal.open(userData);
            }

            if (deleteBtn) {
                event.preventDefault();
                const confirmed = await deleteModal.confirm();

                if (confirmed) {
                    const form = deleteBtn.closest('form');
                    if (form) form.submit();
                }
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

        setTimeout(() => {
            alert.remove();
        }, 500);
    }, duration);
}

function createEditModal(modalId, formId) {
    const modal = document.getElementById(modalId);
    const form = document.getElementById(formId);

    if (!modal || !form) return { open: () => {} };

    const inputs = {
        firstName: form.querySelector('[name="firstName"]'),
        lastName: form.querySelector('[name="lastName"]'),
        email: form.querySelector('[name="email"]'),
        role: form.querySelector('[name="role"]')
    };

    const close = () => {
        modal.classList.remove('modal--active');
        form.reset();
    };

    const open = (userData) => {
        form.action = `/admin/users/${userData.id}/update`;

        let firstName = userData.firstName || '';
        let lastName = userData.lastName || '';

        if (!firstName && userData.fullName) {
            const parts = userData.fullName.trim().split(/\s+/);
            firstName = parts[0] || '';
            lastName = parts.slice(1).join(' ') || '';
        }

        inputs.firstName.value = firstName;
        inputs.lastName.value = lastName;
        inputs.email.value = userData.email || '';
        inputs.role.value = userData.role || 'USER';

        modal.classList.add('modal--active');
    };

    bindModalEvents(modal, close);

    return { open, close };
}

function createDeleteModal(modalId, confirmBtnId) {
    const modal = document.getElementById(modalId);
    const confirmBtn = document.getElementById(confirmBtnId);
    let resolvePromise = null;

    if (!modal) return { confirm: () => Promise.resolve(false) };

    const close = (result = false) => {
        modal.classList.remove('modal--active');
        if (resolvePromise) {
            resolvePromise(result);
            resolvePromise = null;
        }
    };

    const confirm = () => {
        modal.classList.add('modal--active');
        return new Promise((resolve) => {
            resolvePromise = resolve;
        });
    };

    bindModalEvents(modal, () => close(false));

    if (confirmBtn) {
        confirmBtn.addEventListener('click', () => close(true));
    }

    return { confirm };
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