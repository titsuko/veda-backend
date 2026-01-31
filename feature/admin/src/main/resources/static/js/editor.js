class ContentEditor {
    constructor(config) {
        this.config = {
            cardId: config.cardId,
            initialData: config.initialData || [],
            api: {
                save: `/admin/cards/content/${config.cardId}/save`
            },
            dom: {
                editorId: 'editorjs',
                containerId: 'editorContainer',
                saveBtnId: 'saveBtn',
                statusId: 'saveStatus'
            },
            ...config
        };

        this.editor = null;
        this.dom = this._cacheDom();
        this.init();
    }

    _cacheDom() {
        const d = this.config.dom;
        return {
            container: document.getElementById(d.containerId),
            saveBtn: document.getElementById(d.saveBtnId),
            status: document.getElementById(d.statusId)
        };
    }

    init() {
        if (!this._checkDependencies()) return;
        this._initEditorInstance();
        this._attachEventListeners();
    }

    _checkDependencies() {
        if (typeof EditorJS === 'undefined' || typeof SimpleImage === 'undefined') {
            console.error('[ContentEditor] Libraries missing');
            return false;
        }
        return true;
    }

    _initEditorInstance() {
        try {
            this.editor = new EditorJS({
                holder: this.config.dom.editorId,
                placeholder: 'Type here or press Tab...',
                autofocus: true,
                minHeight: 0,
                data: {
                    blocks: this.config.initialData.map(BlockMapper.toEditor).filter(Boolean)
                },
                tools: this._getToolsConfig(),
                onChange: () => this._updateStatus('Unsaved changes...', 'warning')
            });
        } catch (error) {
            console.error('[ContentEditor] Init failed:', error);
        }
    }

    _getToolsConfig() {
        return {
            header: {
                class: Header,
                config: { placeholder: 'Heading', levels: [1, 2, 3], defaultLevel: 2 },
                shortcut: 'CMD+SHIFT+H'
            },
            paragraph: {
                class: Paragraph,
                inlineToolbar: true
            },
            image: {
                class: SimpleImage,
                inlineToolbar: true,
                toolbox: {
                    title: 'Image',
                    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24"><path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m3 16 5-7 6 6.5m6.5 2.5L16 13l-4.286 6M14 10h.01M4 19h16a1 1 0 0 0 1-1V6a1 1 0 0 0-1-1H4a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1Z"/></svg>'
                }
            }
        };
    }

    _attachEventListeners() {
        if (this.dom.saveBtn) {
            this.dom.saveBtn.addEventListener('click', () => this.save());
        }
        if (this.dom.container) {
            this.dom.container.addEventListener('click', (e) => {
                if (e.target.id === this.config.dom.containerId) {
                    this.editor.caret.setToLastBlock();
                }
            });
        }
    }

    async save() {
        if (!this.editor) return;

        this._setLoadingState(true);
        this._updateStatus('Saving...', 'neutral');

        try {
            const outputData = await this.editor.save();
            const payload = outputData.blocks.map(BlockMapper.toBackend).filter(Boolean);

            const response = await fetch(this.config.api.save, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errorText = await response.text();
                if (response.status === 401) throw new Error('Unauthorized');
                if (response.status === 403) throw new Error('Forbidden');
                throw new Error(`Server error: ${response.status}`);
            }

            this._updateStatus('Saved', 'success');
            setTimeout(() => this._updateStatus('', 'neutral'), 3000);

        } catch (error) {
            console.error('[ContentEditor] Save error:', error);
            this._updateStatus('Save failed', 'error');
            alert(error.message || 'Failed to save.');
        } finally {
            this._setLoadingState(false);
        }
    }

    _updateStatus(text, type = 'neutral') {
        if (!this.dom.status) return;
        const colors = { success: '#10b981', error: '#ef4444', warning: '#d97706', neutral: '#6b7280' };
        this.dom.status.textContent = text;
        this.dom.status.style.color = colors[type] || colors.neutral;
        this.dom.status.style.opacity = text ? '1' : '0';
    }

    _setLoadingState(isLoading) {
        if (!this.dom.saveBtn) return;
        this.dom.saveBtn.disabled = isLoading;
        this.dom.saveBtn.textContent = isLoading ? 'Saving...' : 'Save Changes';
    }
}

class BlockMapper {
    static toEditor(block) {
        if (!block || !block.type) return null;

        switch (block.type) {
            case 'header':
                return {
                    type: 'header',
                    data: {
                        text: block.text || '',
                        level: block.level || 2
                    }
                };
            case 'text':
                return {
                    type: 'paragraph',
                    data: {
                        text: block.text || ''
                    }
                };
            case 'image':
                return {
                    type: 'image',
                    data: {
                        url: block.url || '',
                        caption: block.caption || ''
                    }
                };
            default:
                return null;
        }
    }

    static toBackend(block) {
        if (!block || !block.type || !block.data) return null;

        switch (block.type) {
            case 'header':
                return {
                    type: 'header',
                    text: block.data.text || '',
                    level: block.data.level
                };
            case 'paragraph':
                return {
                    type: 'text',
                    text: block.data.text || ''
                };
            case 'image':
                return {
                    type: 'image',
                    url: block.data.url || '',
                    caption: block.data.caption || ''
                };
            default:
                return null;
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const cardId = typeof CARD_ID !== 'undefined' ? CARD_ID : null;
    const initialData = typeof SERVER_DATA !== 'undefined' ? SERVER_DATA : [];

    if (cardId) {
        new ContentEditor({ cardId, initialData });
    }
});