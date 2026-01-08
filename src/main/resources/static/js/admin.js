
// Admin Logic

async function loadUsers() {
    const res = await fetch('/api/admin/users');
    if (res.ok) {
        const users = await res.json();
        const tbody = document.getElementById('admin-user-list');
        if (!tbody) return;
        tbody.innerHTML = users.map(u => `
            <tr>
                <td>${u.id}</td>
                <td>${u.username}</td>
                <td>${u.role}</td>
                <td>${u.score}</td>
                <td>
                    <button onclick="toggleUserVip(${u.id}, ${!u.isVip})" style="background-color: ${u.isVip ? '#28a745' : '#6c757d'}; border: 2px solid #1a1a1a; padding: 5px 10px; color: white; cursor: pointer;">
                        ${u.isVip ? '已激活' : '未激活'}
                    </button>
                </td>
                <td>
                    <button class="action-btn" onclick="openAdminEditUserModal(${u.id})">编辑</button>
                    <button class="action-btn danger" onclick="deleteUser(${u.id})">删除</button>
                </td>
            </tr>
        `).join('');
    }
}

async function toggleUserVip(id, isVip) {
    const formData = new FormData();
    formData.append('isVip', isVip);
    
    try {
        const res = await fetch(`/api/admin/users/${id}/vip`, {
            method: 'POST',
            body: formData
        });
        const result = await res.json();
        if (result.success) {
            loadUsers(); 
        } else {
            alert('操作失败: ' + result.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('操作失败');
    }
}

async function deleteUser(id) {
    if(!confirm('确定要删除该用户吗？')) return;
    await fetch(`/api/admin/users/${id}`, { method: 'DELETE' });
    loadUsers();
}

async function openAdminEditUserModal(id) {
    const res = await fetch(`/api/admin/users/${id}`);
    const result = await res.json();
    
    if (result.success) {
        const u = result.data;
        document.getElementById('u-id').value = u.id;
        document.getElementById('u-username').value = u.username;
        document.getElementById('u-password').value = ''; 
        document.getElementById('u-role').value = u.role;
        document.getElementById('u-score').value = u.score;
        document.getElementById('u-isvip').checked = u.isVip || false;
        
        document.getElementById('userModal').style.display = 'block';
    } else {
        alert(result.message);
    }
}

async function saveUser() {
    const id = document.getElementById('u-id').value;
    const username = document.getElementById('u-username').value;
    const password = document.getElementById('u-password').value;
    const role = document.getElementById('u-role').value;
    const score = document.getElementById('u-score').value;
    const isVip = document.getElementById('u-isvip').checked;
    
    const formData = new FormData();
    formData.append('username', username);
    formData.append('role', role);
    formData.append('score', score);
    formData.append('isVip', isVip);
    
    if (password) {
        formData.append('password', password);
    }
    
    try {
        const response = await fetch(`/api/admin/users/${id}`, {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        if (result.success) {
            alert('用户更新成功！');
            document.getElementById('userModal').style.display = 'none';
            loadUsers();
        } else {
            alert('用户更新失败: ' + result.message);
        }
    } catch (error) {
        console.error('Update error:', error);
        alert('更新失败，请检查网络。');
    }
}

async function loadChallenges() {
    const res = await fetch('/api/challenges?size=1000');
    if (res.ok) {
        const data = await res.json();
        const challenges = data.challenges || data;
        const tbody = document.getElementById('admin-challenge-list');
        if (!tbody) return;
        tbody.innerHTML = challenges.map(c => `
            <tr>
                <td>${c.id}</td>
                <td>${c.title}</td>
                <td>${c.categoryName || c.categoryId}</td>
                <td>${c.points}</td>
                <td>${c.isVip ? '✅' : '❌'}</td>
                <td>
                    <input type="number" value="${c.sortOrder || 0}" style="width: 60px; padding: 2px; background: white; color: #333; border: 1px solid #1a1a1a;" 
                           onchange="updateSortOrder(${c.id}, this.value)">
                </td>
                <td>
                    <button class="action-btn" onclick="openAdminEditChallenge(${c.id})">编辑</button>
                    <button class="action-btn danger" onclick="deleteChallenge(${c.id})">删除</button>
                </td>
            </tr>
        `).join('');
    }
}

async function openAdminEditChallenge(id) {
    const res = await fetch(`/api/admin/challenges/${id}`);
    const result = await res.json();
    
    if (result.success) {
        const c = result.data;
        document.getElementById('c-modal-title').innerText = '编辑题目';
        document.getElementById('c-id').value = c.id;
        document.getElementById('c-title').value = c.title;
        document.getElementById('c-desc').value = c.description;
        document.getElementById('c-flag').value = c.flag;
        document.getElementById('c-points').value = c.points;
        document.getElementById('c-cat').value = c.categoryId;
        document.getElementById('c-isvip').checked = c.isVip || false;
        
        const attachmentDiv = document.getElementById('c-current-attachment');
        const deleteCheckboxLabel = document.getElementById('c-delete-attachment-label');
        const fileInput = document.getElementById('c-file');
        
        fileInput.value = '';
        document.getElementById('c-delete-attachment').checked = false;
        
        if (c.attachmentUrl) {
            const fileName = c.attachmentUrl.split('/').pop();
            attachmentDiv.innerHTML = `当前附件: <a href="${c.attachmentUrl}" target="_blank" style="color: #007bff;">${fileName}</a>`;
            deleteCheckboxLabel.style.display = 'block';
        } else {
            attachmentDiv.innerHTML = '当前无附件';
            deleteCheckboxLabel.style.display = 'none';
        }
        
        document.getElementById('challengeModal').style.display = 'block';
    } else {
        alert(result.message);
    }
}

async function saveChallenge() {
    const id = document.getElementById('c-id').value;
    const title = document.getElementById('c-title').value;
    const description = document.getElementById('c-desc').value;
    const flag = document.getElementById('c-flag').value;
    const points = document.getElementById('c-points').value;
    const categoryId = document.getElementById('c-cat').value;
    const isVip = document.getElementById('c-isvip').checked;
    const fileInput = document.getElementById('c-file');
    
    const formData = new FormData();
    formData.append('title', title);
    formData.append('description', description);
    formData.append('flag', flag);
    formData.append('points', points);
    formData.append('categoryId', categoryId);
    formData.append('isVip', isVip);
    
    if (fileInput.files.length > 0) {
        formData.append('file', fileInput.files[0]);
    }
    
    let url = '/api/admin/challenges';
    let method = 'POST';
    
    if (id) {
        // Update
        url = `/api/admin/challenges/${id}`;
        const deleteAttachment = document.getElementById('c-delete-attachment').checked;
        formData.append('deleteAttachment', deleteAttachment);
    }
    
    try {
        const response = await fetch(url, {
            method: method,
            body: formData
        });
        
        const result = await response.json();
        if (result.success) {
            alert(id ? '题目更新成功！' : '题目添加成功！');
            document.getElementById('challengeModal').style.display = 'none';
            loadChallenges();
        } else {
            alert('操作失败: ' + result.message);
        }
    } catch (error) {
        console.error('Save error:', error);
        alert('操作失败，请检查网络。');
    }
}

async function updateSortOrder(id, sortOrder) {
    const formData = new FormData();
    formData.append('sortOrder', sortOrder);
    await fetch(`/api/admin/challenges/${id}/sort`, {
        method: 'POST',
        body: formData
    });
}

async function deleteChallenge(id) {
    if(!confirm('确定要删除该题目吗？')) return;
    await fetch(`/api/admin/challenges/${id}`, { method: 'DELETE' });
    loadChallenges();
}
