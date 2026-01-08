let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    checkLoginStatus();
    
    const categoryItems = document.querySelectorAll('.category-item');
    
    // 分类点击事件
    categoryItems.forEach(item => {
        item.addEventListener('click', async () => {
            categoryItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            
            const categoryId = item.dataset.id;
            const url = categoryId === 'all' ? '/api/challenges' : `/api/challenges?categoryId=${categoryId}`;
            
            const response = await fetch(url);
            const challenges = await response.json();
            renderChallenges(challenges);
        });
    });
});

function renderChallenges(challenges) {
    const challengeList = document.getElementById('challenge-list');
    challengeList.innerHTML = challenges.map(c => `
        <div class="challenge-card" id="challenge-${c.id}">
            <div class="challenge-header">
                <h4 class="challenge-title">${c.title}</h4>
                <div class="challenge-meta">
                    <span class="points-badge">${c.points} 分</span>
                    <span>[${c.categoryName || 'General'}]</span>
                </div>
            </div>
            <p class="challenge-desc">${c.description}</p>
            ${(() => {
                if (!c.attachmentUrl) return '';
                const isImage = /\.(jpg|jpeg|png|gif|webp)$/i.test(c.attachmentUrl);
                let html = '';
                if (isImage) {
                    const filename = c.attachmentUrl.split('/').pop();
                    const thumbUrl = `/api/thumbnail/${filename}`;
                    html += `
                        <div class="attachment-box">
                            <img src="${thumbUrl}" alt="Attachment Thumbnail" class="attachment-thumb" loading="lazy" data-url="${c.attachmentUrl}" onclick="window.open(this.dataset.url)" onerror="this.onerror=null;this.src=this.dataset.url">
                            <a href="${c.attachmentUrl}" target="_blank" class="attachment-link">[查看原图]</a>
                        </div>
                    `;
                }
                html += `<p><a href="${c.attachmentUrl}" target="_blank" class="attachment-link" download>[下载附件]</a></p>`;
                return html;
            })()}
            
            ${(() => {
                const webLinks = {
                    'SQL 注入': '/challenge/web/sqli',
                    'Cookie 伪造': '/challenge/web/cookie',
                    '机器人协议': '/robots.txt'
                };
                for (const [key, url] of Object.entries(webLinks)) {
                    if (c.title && c.title.includes(key)) {
                         return `<div style="margin-top:10px; margin-bottom: 10px;">
                                    <a href="${url}" target="_blank" style="display:inline-block; padding:8px 15px; background:#00ff00; color:#000; text-decoration:none; font-weight:bold; border: 1px solid #000; box-shadow: 2px 2px 0 #004400;">
                                        ⚡ 启动靶机 (Launch Instance)
                                    </a>
                                 </div>`;
                    }
                }
                return '';
            })()}

            <div class="flag-input-group">
                <input type="text" placeholder="ctf{...}" id="input-${c.id}">
                <button onclick="submitFlag(${c.id})">提交</button>
            </div>
            <div class="message" id="msg-${c.id}"></div>
        </div>
    `).join('');
}

// Auth Logic
async function checkLoginStatus() {
    const res = await fetch('/api/auth/me');
    const data = await res.json();
    const controls = document.getElementById('auth-controls');
    
    if (data.success) {
        currentUser = data.user;
        let html = `<span>欢迎, ${currentUser.username} (${currentUser.score} 分)</span>`;
        html += `<a href="javascript:void(0)" onclick="openModal('profileModal')">个人档案</a>`;
        
        if (currentUser.role === 'ADMIN') {
            html += `<a href="javascript:void(0)" onclick="openModal('adminModal')">管理后台</a>`;
        }
        
        html += `<a href="javascript:void(0)" onclick="logout()">退出</a>`;
        controls.innerHTML = html;
        updateProfileContent();
    } else {
        currentUser = null;
        controls.innerHTML = `
            <a href="javascript:void(0)" onclick="openModal('loginModal')">登录</a>
            <a href="javascript:void(0)" onclick="openModal('registerModal')">注册</a>
        `;
    }
}

async function login() {
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    const msg = document.getElementById('login-msg');
    
    const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
    });
    const data = await res.json();
    
    if (data.success) {
        closeModal('loginModal');
        checkLoginStatus();
        window.location.reload(); 
    } else {
        msg.textContent = data.message;
        msg.className = 'message error';
    }
}

async function register() {
    const username = document.getElementById('reg-username').value;
    const password = document.getElementById('reg-password').value;
    const msg = document.getElementById('reg-msg');
    
    const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
    });
    const data = await res.json();
    
    if (data.success) {
        msg.textContent = '注册成功！请登录。';
        msg.className = 'message success';
        setTimeout(() => {
            closeModal('registerModal');
            openModal('loginModal');
        }, 1500);
    } else {
        msg.textContent = data.message;
        msg.className = 'message error';
    }
}

async function logout() {
    await fetch('/api/auth/logout', { method: 'POST' });
    window.location.reload();
}

function updateProfileContent() {
    if (!currentUser) return;
    const content = document.getElementById('profile-content');
    content.innerHTML = `
        <p><strong>用户名:</strong> ${currentUser.username}</p>
        <p><strong>角色:</strong> ${currentUser.role}</p>
        <p><strong>积分:</strong> ${currentUser.score}</p>
        <p><strong>VIP 状态:</strong> ${currentUser.isVip ? '已激活' : '未激活'}</p>
    `;
}

// Modal Logic
function openModal(id) {
    document.getElementById(id).style.display = "block";
    if (id === 'adminModal') loadUsers();
}

function closeModal(id) {
    document.getElementById(id).style.display = "none";
}

window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.style.display = "none";
    }
}

// Admin Logic
async function loadUsers() {
    const res = await fetch('/api/admin/users');
    if (res.ok) {
        const users = await res.json();
        const tbody = document.getElementById('admin-user-list');
        tbody.innerHTML = users.map(u => `
            <tr>
                <td>${u.id}</td>
                <td>${u.username}</td>
                <td>${u.role}</td>
                <td>${u.score}</td>
                <td>
                    <button class="btn-danger" onclick="deleteUser(${u.id})">删除</button>
                </td>
            </tr>
        `).join('');
    }
}

async function deleteUser(id) {
    if(!confirm('确定要删除该用户吗？')) return;
    await fetch(`/api/admin/users/${id}`, { method: 'DELETE' });
    loadUsers();
}

async function loadChallenges() {
    const res = await fetch('/api/challenges');
    if (res.ok) {
        const challenges = await res.json();
        const tbody = document.getElementById('admin-challenge-list');
        tbody.innerHTML = challenges.map(c => `
            <tr>
                <td>${c.id}</td>
                <td>${c.title}</td>
                <td>${c.categoryName || c.categoryId}</td>
                <td>${c.points}</td>
                <td>
                    <input type="number" value="${c.sortOrder || 0}" style="width: 60px; padding: 2px;" 
                           onchange="updateSortOrder(${c.id}, this.value)">
                </td>
                <td>
                    <button class="btn-danger" onclick="deleteChallenge(${c.id})">删除</button>
                </td>
            </tr>
        `).join('');
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
    // Also refresh the main challenge list if needed, but page reload is simple
    // window.location.reload(); 
}

async function addChallenge() {
    const title = document.getElementById('admin-c-title').value;
    const description = document.getElementById('admin-c-desc').value;
    const flag = document.getElementById('admin-c-flag').value;
    const points = document.getElementById('admin-c-points').value;
    const categoryId = document.getElementById('admin-c-cat').value;
    const fileInput = document.getElementById('admin-c-file');
    
    const formData = new FormData();
    formData.append('title', title);
    formData.append('description', description);
    formData.append('flag', flag);
    formData.append('points', points);
    formData.append('categoryId', categoryId);
    
    if (fileInput.files.length > 0) {
        formData.append('file', fileInput.files[0]);
    }
    
    try {
        const response = await fetch('/api/admin/challenges', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        if (result.success) {
            alert('题目添加成功！');
            window.location.reload();
        } else {
            alert('题目添加失败: ' + result.message);
        }
    } catch (error) {
        console.error('Upload error:', error);
        alert('上传失败，请检查网络或文件大小是否超出服务器限制。');
    }
}

// Existing Logic
async function submitFlag(id) {
    if (!currentUser) {
        alert('请先登录！');
        openModal('loginModal');
        return;
    }

    const flagInput = document.getElementById(`input-${id}`);
    const msgDiv = document.getElementById(`msg-${id}`);
    const flag = flagInput.value.trim();

    if (!flag) return;

    try {
        const response = await fetch('/api/verify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                id: id, 
                flag: flag,
                userId: currentUser.id 
            })
        });
        const result = await response.json();

        msgDiv.textContent = result.message;
        msgDiv.className = 'message ' + (result.success ? 'success' : 'error');
        
        if (result.success) {
            flagInput.disabled = true;
            checkLoginStatus(); // Update score in UI
            refreshRankings();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function recharge() {
    if (!currentUser) {
        alert('请先登录');
        return;
    }
    const msgDiv = document.getElementById('recharge-msg');
    try {
        const response = await fetch('/api/recharge', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: currentUser.id })
        });
        const result = await response.json();
        msgDiv.textContent = result.message;
        msgDiv.className = 'message success';
        checkLoginStatus();
        refreshRankings();
    } catch (error) {
        console.error('Error:', error);
    }
}

async function refreshRankings() {
    const response = await fetch('/api/rankings');
    const rankings = await response.json();
    const rankBody = document.getElementById('rank-body');
    rankBody.innerHTML = rankings.map((user, index) => `
        <tr>
            <td>${index + 1}</td>
            <td>
                ${user.username}
                ${user.isVip ? '<span class="vip-tag">VIP</span>' : ''}
            </td>
            <td>${user.score}</td>
        </tr>
    `).join('');
}
