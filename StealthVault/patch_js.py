import sys

with open('DEMO.html', 'r', encoding='utf-8') as f:
    lines = f.read().split('\n')

start_js = -1
end_js = -1
for i, line in enumerate(lines):
    if 'function renderCategories()' in line:
        start_js = i
    if 'function updateStats()' in line:
        end_js = i

if start_js == -1 or end_js == -1:
    print("JS Markers not found!")
    sys.exit(1)

new_js = """        function renderCategories() {
            const nav = document.getElementById('categoryNav');
            nav.innerHTML = '';
            state.categories.forEach(cat => {
                const btn = document.createElement('button');
                btn.onclick = () => {
                    state.activeCategory = cat.id;
                    document.getElementById('categoryTitle').innerText = cat.label;
                    renderCategories(); renderVault();
                };
                const isActive = state.activeCategory === cat.id;
                btn.className = `w-full flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all text-sm group font-semibold ${isActive ? 'bg-blue-50 text-blue-600 border border-blue-100/50' : 'text-slate-500 hover:text-slate-900 border border-transparent'}`;
                btn.innerHTML = `<i data-lucide="${isActive ? 'check-circle' : cat.icon}" class="w-[18px] h-[18px] ${isActive ? 'text-blue-600' : 'text-slate-400 group-hover:text-slate-600'}"></i><span>${cat.id === 'All' ? 'Dashboard' : cat.id}</span>`;
                nav.appendChild(btn);
            });
            lucide.createIcons();
        }

        function renderVault() {
            const grid = document.getElementById('vaultGrid');
            const search = document.getElementById('searchInput').value.toLowerCase();
            const filtered = state.items.filter(item => {
                const matchesSearch = item.title.toLowerCase().includes(search) || item.user.toLowerCase().includes(search);
                const matchesCat = state.activeCategory === 'All' || item.category === state.activeCategory;
                return matchesSearch && matchesCat;
            });
            grid.innerHTML = '';
            if (filtered.length === 0) {
                grid.innerHTML = `<tr><td colspan="5" class="py-12"><div class="flex flex-col items-center justify-center text-slate-400 w-full animate-fade"><div class="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mb-4 border border-slate-100"><i data-lucide="ghost" class="w-8 h-8 text-slate-300"></i></div><p class="font-bold text-sm text-slate-600 mb-1">No Vault Entries</p></div></td></tr>`;
            } else {
                filtered.forEach((item, idx) => {
                    const tr = document.createElement('tr');
                    tr.className = "hover:bg-slate-50/80 transition-colors group animate-slide-up";
                    tr.style.animationDelay = `${idx * 0.03}s`;
                    const isStrong = item.pass.length > 12;
                    tr.innerHTML = `
                        <td class="py-4 px-6"><div class="flex items-center gap-3"><span class="text-[10px] font-mono text-slate-400 font-bold bg-slate-50 px-1.5 py-0.5 rounded border border-slate-100">#${item.id.slice(-5)}</span><div class="font-bold text-slate-900 group-hover:text-blue-600 transition-colors whitespace-nowrap text-sm">${item.title}</div></div></td>
                        <td class="py-4 px-6"><div class="text-xs font-medium text-slate-600 truncate max-w-[120px]">${item.user}</div></td>
                        <td class="py-4 px-6"><div class="flex items-center gap-2"><code id="pass-${item.id}" class="text-xs font-mono text-slate-500 font-bold tracking-wider">••••••••••••</code><button onclick="toggleCardPass('${item.id}')" title="Reveal" class="text-slate-400 hover:text-blue-600"><i data-lucide="eye" class="w-4 h-4"></i></button></div></td>
                        <td class="py-4 px-6"><span class="text-[10px] font-bold px-2.5 py-1 rounded-full flex items-center gap-1 w-max ${isStrong ? 'bg-emerald-50 text-emerald-600' : 'bg-red-50 text-red-600'}">${isStrong ? '<i data-lucide="shield-check" class="w-3 h-3"></i> Strong' : '<i data-lucide="alert-triangle" class="w-3 h-3"></i> Weak'}</span></td>
                        <td class="py-4 px-6 text-right"><div class="flex items-center justify-end gap-2"><button onclick="copyPass('${item.id}')" title="Copy" class="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg"><i data-lucide="copy" class="w-4 h-4"></i></button><button onclick="deleteItem('${item.id}')" title="Delete" class="p-2 text-slate-400 hover:text-rose-500 hover:bg-rose-50 rounded-lg"><i data-lucide="trash-2" class="w-4 h-4"></i></button></div></td>`;
                    grid.appendChild(tr);
                });
            }
            lucide.createIcons(); updateStats();
        }
"""

new_lines = lines[:start_js] + new_js.split('\n') + lines[end_js:]

with open('DEMO.html', 'w', encoding='utf-8') as f:
    f.write('\n'.join(new_lines))
