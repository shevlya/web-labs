import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {HttpClient, HttpHeaders, HttpErrorResponse} from '@angular/common/http';
import {firstValueFrom, Observable, throwError} from 'rxjs';
import {catchError, take} from 'rxjs/operators';
import {environment} from '../../environment';
import {
  AdminEvent, AdminStatistics, CatalogItem, ConfirmConfig, DraftField,
  OrganizerRequestShort, ROLE_LABELS, USER_STATUS_LABELS, USER_STATUSES, UserResponse
} from "../../models/admin.models";
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {
  private readonly BASE = environment.apiUrl;
  private readonly RECENT_LIMIT = 5;
  private readonly DEFAULT_COLOR = '#9c89ff';

  private readonly API = {
    STATS: '/admin/statistics',
    EVENTS: '/events',
    REQUESTS: '/organizer-requests',
    USERS: '/users',
    CATEGORIES: '/categories',
    ROLES: '/roles',
    PHYSICAL_PLACES: '/physical-places',
    ONLINE_PLACES: '/online-places',
    verifyEvent: (id: number) => `/events/${id}/verify`,
    reviewRequest: (id: number) => `/organizer-requests/${id}/review`,
    approveChanges: (id: number) => `/events/admin/${id}/approve`,
    rejectChanges: (id: number) => `/events/admin/${id}/reject`,
    deleteCatalog: (endpoint: string, id: number) => `${endpoint}/${id}`
  } as const;

  private readonly STATUS_NAMES: Record<string, string> = {
    'PENDING': 'На рассмотрении', 'APPROVED': 'Одобрено', 'REJECTED': 'Отклонено',
    'ONGOING': 'Идёт', 'COMPLETED': 'Завершено', 'CANCELLED': 'Отменено',
    'PLANNED': 'Запланировано', 'PUBLISHED': 'Опубликовано'
  };

  private readonly EVENT_FORMAT_LABELS: Record<string, string> = {
    'ONLINE': 'Онлайн',
    'OFFLINE': 'Офлайн',
    'HYBRID': 'Гибридный',
  };

  activeRoleSelectUserId: number | null = null;
  activeStatusSelectUserId: number | null = null;

  activeTab = 'dashboard';
  sidebarCollapsed = false;
  loading = false;
  stats: AdminStatistics | null = null;
  roleLabels = ROLE_LABELS;

  allEvents: AdminEvent[] = [];
  filteredEvents: AdminEvent[] = [];
  eventFilter: 'pending' | 'changes' | 'all' = 'pending';

  allRequests: OrganizerRequestShort[] = [];
  filteredRequests: OrganizerRequestShort[] = [];
  requestFilter: 'pending' | 'all' = 'pending';

  allUsers: UserResponse[] = [];
  filteredUsers: UserResponse[] = [];
  userSearchQuery = '';

  activeCatalogTab = 'categories';
  catalogTabs = [
    {key: 'categories', label: 'Категории'},
    {key: 'roles', label: 'Роли'}
  ] as const;
  catalogData: Record<string, CatalogItem[]> = {categories: [], roles: []};
  catalogForm: FormGroup;
  catalogSideOpen = false;
  editingItem: CatalogItem | null = null;
  readonly colorPresets = [
    '#9c89ff', '#f97316', '#22c55e', '#ef4444', '#eab308',
    '#3b82f6', '#ec4899', '#14b8a6', '#8b5cf6', '#64748b'
  ];

  confirmModal: ConfirmConfig = {
    visible: false, title: '', message: '', confirmLabel: 'Подтвердить',
    type: 'approve', showComment: false, onConfirm: () => {
    }
  };
  viewModal = {visible: false, title: '', text: ''};
  compareModal = {
    visible: false, eventId: null as number | null, eventName: '',
    fields: [] as DraftField[], loading: false, comment: ''
  };

  currentUserId: number | null = null;
  userStatuses = USER_STATUSES;
  userStatusLabels = USER_STATUS_LABELS;
  availableRoles: CatalogItem[] = [];

  private readonly cache = new Map<string, any>();

  constructor(
    private http: HttpClient,
    private router: Router,
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.catalogForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      colorCode: [this.DEFAULT_COLOR, [Validators.pattern('^#([A-Fa-f0-9]{6})$')]]
    });
  }

  async ngOnInit() {
    await this.loadCurrentUserPromise();
    this.loadAll();
  }

  private loadCurrentUserPromise(): Promise<void> {
    return new Promise((resolve) => {
      const user = this.authService.getCurrentUser();
      if (user) {
        this.currentUserId = Number(user.userId);
        resolve();
      } else {
        // Если пользователь ещё не загружен — ждём из стрима
        this.authService.user$.pipe(take(1)).subscribe({
          next: u => {
            this.currentUserId = u ? Number(u.userId) : null;
            resolve();
          },
          error: () => {
            this.currentUserId = null;
            resolve();
          }
        });
      }
    });
  }

  private get headers(): HttpHeaders {
    const token = this.authService.getToken() ?? '';
    return new HttpHeaders({Authorization: `Bearer ${token}`});
  }

  private request<T>(url: string): Observable<T> {
    return this.http.get<T>(url, {headers: this.headers}).pipe(
      catchError((err: HttpErrorResponse) => {
        console.error(`Request error ${url}`, err);
        return throwError(() => err);
      })
    );
  }

  loadAll() {
    this.loadStats();
    this.loadEvents();
    this.loadRequests();
    this.loadUsers();
    this.loadCatalog();
  }

  setTab(tab: string) {
    this.activeTab = tab;
    if (tab !== 'catalog') this.closeCatalogForm();
  }

  loadStats() {
    this.request<AdminStatistics>(`${this.BASE}${this.API.STATS}`).subscribe(d => this.stats = d);
  }

  loadEvents() {
    this.loading = true;
    this.request<AdminEvent[]>(`${this.BASE}${this.API.EVENTS}`).subscribe({
      next: data => {
        this.allEvents = data;
        this.applyEventFilter();
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  setEventFilter(f: 'pending' | 'changes' | 'all') {
    this.eventFilter = f;
    this.applyEventFilter();
  }

  private applyEventFilter() {
    switch (this.eventFilter) {
      case 'pending':
        this.filteredEvents = this.allEvents.filter(e => !e.verified);
        break;
      case 'changes':
        this.filteredEvents = this.allEvents.filter(e => e.moderationStatus === 'PENDING');
        break;
      default:
        this.filteredEvents = [...this.allEvents];
    }
  }

  verifyEvent(id: number, approve: boolean) {
    this.openConfirm({
      title: approve ? 'Одобрить мероприятие?' : 'Отклонить мероприятие?',
      message: approve ? 'Мероприятие будет опубликовано для пользователей.' : 'Мероприятие будет скрыто от пользователей.',
      type: approve ? 'approve' : 'reject',
      confirmLabel: approve ? 'Одобрить' : 'Отклонить',
      showComment: true,
      onConfirm: () => {
        const comment = this.confirmModal.comment;
        const url = `${this.BASE}${this.API.verifyEvent(id)}?verified=${approve}&sendEmail=true`;
        const body = comment ? {verificationComment: comment} : {};
        this.http.patch(url, body, {headers: this.headers}).subscribe(() => {
          this.loadEvents();
          this.loadStats();
          this.confirmModal.visible = false;
        });
      }
    });
  }

  viewEvent(id: number) {
    this.router.navigate(['/events', id]);
  }

  private async getCached<T>(key: string, loader: () => Promise<T>): Promise<T> {
    if (this.cache.has(key)) return this.cache.get(key);
    const value = await loader();
    this.cache.set(key, value);
    return value;
  }

  private async loadPlaceInfo(placeId: number | null): Promise<string> {
    if (!placeId) return 'Не указано';
    return this.getCached(`place:${placeId}`, async () => {
      try {
        const physical = await firstValueFrom(this.http.get<any>(`${this.BASE}${this.API.PHYSICAL_PLACES}/${placeId}`, {headers: this.headers}));
        if (physical) return `${physical.placeName} (${physical.address})`;
      } catch {
      }
      try {
        const online = await firstValueFrom(this.http.get<any>(`${this.BASE}${this.API.ONLINE_PLACES}/${placeId}`, {headers: this.headers}));
        if (online) return `${online.placeName} (${online.meetingUrl})`;
      } catch {
      }
      return `Место #${placeId}`;
    });
  }

  private async loadCategoryName(categoryId: number | null): Promise<string> {
    if (!categoryId) return 'Не указано';
    return this.getCached(`category:${categoryId}`, async () => {
      try {
        const category = await firstValueFrom(this.http.get<any>(`${this.BASE}${this.API.CATEGORIES}/${categoryId}`, {headers: this.headers}));
        return category.eventCategoryName || `Категория #${categoryId}`;
      } catch {
        return `Категория #${categoryId}`;
      }
    });
  }

  async openCompareModal(eventId: number) {
    this.compareModal = {visible: true, eventId, eventName: '', fields: [], loading: true, comment: ''};
    try {
      const fullEvent = await firstValueFrom(this.http.get<AdminEvent>(`${this.BASE}${this.API.EVENTS}/${eventId}`, {headers: this.headers}));
      if (!fullEvent?.draftChanges) {
        this.compareModal.loading = false;
        return;
      }
      this.compareModal.eventName = fullEvent.eventName;
      const draft = fullEvent.draftChanges;
      const [currentPlaceInfo, newPlaceInfo, currentCategoryName, newCategoryName] = await Promise.all([
        this.loadPlaceInfo(fullEvent.idPlace ?? null),
        this.loadPlaceInfo(draft['placeId'] !== undefined ? draft['placeId'] as number | null : null),
        this.loadCategoryName(fullEvent.idEventCategory ?? null),
        this.loadCategoryName(draft['eventCategoryId'] !== undefined ? draft['eventCategoryId'] as number | null : null)
      ]);
      const fieldMap: Record<string, { label: string; current: any }> = {
        eventName: {label: 'Название', current: fullEvent.eventName},
        eventDescription: {label: 'Описание', current: fullEvent.eventDescription || ''},
        eventDate: {label: 'Дата', current: fullEvent.eventDate},
        startTime: {label: 'Время начала', current: fullEvent.startTime},
        endTime: {label: 'Время окончания', current: fullEvent.endTime},
        maxParticipants: {label: 'Макс. участников', current: fullEvent.maxParticipants},
        imageUrl: {label: 'Изображение', current: fullEvent.imageUrl},
        price: {label: 'Цена', current: fullEvent.price},
        eventFormat: {label: 'Формат', current: fullEvent.eventFormat},
        eventCategoryId: {label: 'Категория', current: currentCategoryName},
        placeId: {label: 'Место', current: currentPlaceInfo}
      };
      const fields: DraftField[] = [];
      for (const [key, newValue] of Object.entries(draft)) {
        const meta = fieldMap[key];
        if (meta) {
          let displayNew = newValue;
          if (key === 'eventCategoryId') displayNew = newCategoryName;
          if (key === 'placeId') displayNew = newPlaceInfo;
          fields.push({key, currentValue: meta.current, newValue: displayNew, label: meta.label, selected: true});
        }
      }
      this.compareModal.fields = fields;
    } catch (e) {
      console.error('Ошибка загрузки события', e);
    } finally {
      this.compareModal.loading = false;
    }
  }

  closeCompareModal() {
    this.compareModal.visible = false;
  }

  approveChanges() {
    const selectedFields = this.compareModal.fields.filter(f => f.selected).map(f => f.key);
    if (!selectedFields.length) {
      alert('Выберите хотя бы одно поле для применения');
      return;
    }
    const url = `${this.BASE}${this.API.approveChanges(this.compareModal.eventId!)}`;
    this.http.post(url, {fields: selectedFields, applyAll: false}, {headers: this.headers}).subscribe(() => {
      this.loadEvents();
      this.closeCompareModal();
    });
  }

  rejectChanges() {
    const url = `${this.BASE}${this.API.rejectChanges(this.compareModal.eventId!)}`;
    const body = this.compareModal.comment ? {comment: this.compareModal.comment} : {};
    this.http.post(url, body, {headers: this.headers}).subscribe(() => {
      this.loadEvents();
      this.closeCompareModal();
    });
  }

  loadRequests() {
    this.request<OrganizerRequestShort[]>(`${this.BASE}${this.API.REQUESTS}`).subscribe(data => {
      this.allRequests = data;
      this.applyRequestFilter();
    });
  }

  setRequestFilter(f: 'pending' | 'all') {
    this.requestFilter = f;
    this.applyRequestFilter();
  }

  private applyRequestFilter() {
    this.filteredRequests = this.requestFilter === 'pending'
      ? this.allRequests.filter(r => r.requestStatus === 'PENDING')
      : [...this.allRequests];
  }

  reviewRequest(id: number, approve: boolean) {
    this.openConfirm({
      title: approve ? 'Одобрить заявку?' : 'Отклонить заявку?',
      message: approve ? 'Пользователь получит роль Организатора.' : 'Заявка будет отклонена.',
      type: approve ? 'approve' : 'reject',
      confirmLabel: approve ? 'Одобрить' : 'Отклонить',
      showComment: true,
      onConfirm: () => {
        const comment = this.confirmModal.comment;
        let url = `${this.BASE}${this.API.reviewRequest(id)}?approved=${approve}&sendEmail=true`;
        if (comment) url += `&reviewComment=${encodeURIComponent(comment)}`;
        this.http.patch(url, null, {headers: this.headers}).subscribe(() => {
          this.loadRequests();
          this.loadStats();
          this.confirmModal.visible = false;
        });
      }
    });
  }

  openViewRequest(request: OrganizerRequestShort) {
    this.viewModal = {visible: true, title: `Заявка от ${request.userFio}`, text: request.requestText};
  }

  closeViewModal() {
    this.viewModal.visible = false;
  }

  getStatusName(status: string): string {
    return this.STATUS_NAMES[status] || status;
  }

  loadUsers() {
    this.request<UserResponse[]>(`${this.BASE}${this.API.USERS}`).subscribe(data => {
      this.allUsers = data;
      this.filteredUsers = data;
    });
  }

  filterUsers() {
    const q = this.userSearchQuery.toLowerCase();
    this.filteredUsers = q ? this.allUsers.filter(u => u.fio.toLowerCase().includes(q) || u.email.toLowerCase().includes(q)) : [...this.allUsers];
  }

  private readonly catalogEndpoints: Record<string, string> = {
    categories: this.API.CATEGORIES,
    roles: this.API.ROLES
  };

  loadCatalog() {
    for (const [key, endpoint] of Object.entries(this.catalogEndpoints)) {
      this.request<any[]>(`${this.BASE}${endpoint}`).subscribe(data => {
        this.catalogData[key] = data.map(item => ({
          id: item.idEventCategory ?? item.idRole,
          name: item.eventCategoryName ?? item.roleName,
          description: item.eventCategoryDescription ?? item.roleDescription ?? '',
          colorCode: item.colorCode ?? null
        }));
        if (key === 'roles') this.availableRoles = this.catalogData['roles'];
      });
    }
  }

  get currentCatalogItems(): CatalogItem[] {
    return this.catalogData[this.activeCatalogTab] ?? [];
  }

  switchCatalogTab(key: string) {
    this.activeCatalogTab = key;
    this.closeCatalogForm();
  }

  openCatalogForm(item: CatalogItem | null) {
    this.editingItem = item;
    this.catalogSideOpen = true;
    if (item) {
      this.catalogForm.patchValue({
        name: item.name,
        description: item.description ?? '',
        colorCode: item.colorCode ?? this.DEFAULT_COLOR
      });
    } else {
      this.catalogForm.reset({name: '', description: '', colorCode: this.DEFAULT_COLOR});
    }
  }

  closeCatalogForm() {
    this.catalogSideOpen = false;
    this.editingItem = null;
    this.catalogForm.reset({name: '', description: '', colorCode: this.DEFAULT_COLOR});
  }

  setColor(hex: string) {
    this.catalogForm.patchValue({colorCode: hex});
  }

  saveCatalogItem() {
    if (this.catalogForm.invalid) {
      this.catalogForm.markAllAsTouched();
      return;
    }
    const {name, description, colorCode} = this.catalogForm.value;
    if (!name?.trim()) return;

    const bodyBuilders: Record<string, (n: string, d: string, c?: string) => any> = {
      categories: (n, d, c) => ({eventCategoryName: n, eventCategoryDescription: d, colorCode: c}),
      roles: (n, d) => ({roleName: n, roleDescription: d})
    };
    const endpoint = this.catalogEndpoints[this.activeCatalogTab];
    const buildBody = bodyBuilders[this.activeCatalogTab];
    if (!endpoint || !buildBody) return;

    const body = buildBody(name.trim(), description?.trim() ?? '', colorCode);
    const request$ = this.editingItem
      ? this.http.put(`${this.BASE}${endpoint}/${this.editingItem.id}`, body, {headers: this.headers})
      : this.http.post(`${this.BASE}${endpoint}`, body, {headers: this.headers});
    request$.subscribe(() => {
      this.loadCatalog();
      this.closeCatalogForm();
    });
  }

  deleteCatalogItem(id: number) {
    const endpoint = this.catalogEndpoints[this.activeCatalogTab];
    if (!endpoint) return;
    this.openConfirm({
      title: 'Удалить запись?', message: 'Это действие необратимо.', type: 'delete',
      confirmLabel: 'Удалить', showComment: false,
      onConfirm: () => {
        this.http.delete(`${this.BASE}${this.API.deleteCatalog(endpoint, id)}`, {headers: this.headers}).subscribe({
          next: () => {
            this.loadCatalog();
            this.confirmModal.visible = false;
            if (this.editingItem?.id === id) this.closeCatalogForm();
          },
          error: (err: HttpErrorResponse) => {
            this.confirmModal.visible = false;
            let errorMessage = 'Не удалось удалить запись';
            if (err.status === 403) errorMessage = 'Доступ запрещён: требуется роль Администратора';
            else if (err.status === 409) errorMessage = 'Невозможно удалить: запись используется';
            else if (err.error?.message) errorMessage = err.error.message;
            alert(errorMessage);
          }
        });
      }
    });
  }

  private openConfirm(config: Partial<ConfirmConfig> & { onConfirm: () => void }) {
    this.confirmModal = {
      visible: true,
      title: '',
      message: '',
      confirmLabel: 'Подтвердить',
      type: 'approve',
      showComment: false,
      comment: '', ...config
    };
  }

  get recentRequests(): OrganizerRequestShort[] {
    return this.allRequests.filter(r => r.requestStatus === 'PENDING').slice(0, this.RECENT_LIMIT);
  }

  get recentPendingEvents(): AdminEvent[] {
    return this.allEvents.filter(e => !e.verified).slice(0, this.RECENT_LIMIT);
  }

  get catalogFormValue() {
    return this.catalogForm.value as { name: string; description: string; colorCode: string };
  }

  updateUserStatus(userId: number, newStatus: string) {
    if (this.currentUserId == userId) {
      alert('Нельзя изменить свой собственный статус');
      return;
    }
    this.http.patch(`${this.BASE}${this.API.USERS}/${userId}/status`, {userStatus: newStatus}, {headers: this.headers})
      .subscribe({
        next: () => this.loadUsers(),
        error: err => alert('Ошибка обновления статуса: ' + (err.error?.message || err.message))
      });
  }

  updateUserRole(userId: number, roleId: number) {
    if (this.currentUserId == userId) {
      alert('Нельзя изменить свою собственную роль');
      return;
    }
    this.http.patch(`${this.BASE}${this.API.USERS}/${userId}/role`, {roleId}, {headers: this.headers})
      .subscribe({
        next: () => this.loadUsers(),
        error: err => alert('Ошибка обновления роли: ' + (err.error?.message || err.message))
      });
  }

  openRoleSelect(userId: number) {
    if (this.currentUserId === userId) return;
    this.activeRoleSelectUserId = userId;
  }

  openStatusSelect(userId: number) {
    if (this.currentUserId === userId) return;
    this.activeStatusSelectUserId = userId;
  }

  closeRoleSelect() {
    this.activeRoleSelectUserId = null;
  }

  closeStatusSelect() {
    this.activeStatusSelectUserId = null;
  }

  onRoleChange(userId: number, roleName: string) {
    const roleId = this.getRoleIdByName(roleName);
    this.updateUserRole(userId, roleId);
    this.closeRoleSelect();
  }

  onStatusChange(userId: number, newStatus: string) {
    this.updateUserStatus(userId, newStatus);
    this.closeStatusSelect();
  }

  getRoleIdByName(roleName: string): number {
    const role = this.availableRoles.find(r => r.name === roleName);
    return role ? role.id : 0;
  }

  getRoleDisplayName(roleName: string): string {
    return this.roleLabels[roleName as keyof typeof this.roleLabels] || roleName;
  }

  getEventFormatLabel(format: string): string {
    return this.EVENT_FORMAT_LABELS[format] || format;
  }
}
