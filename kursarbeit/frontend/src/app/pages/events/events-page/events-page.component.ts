import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';
import {ReactiveFormsModule, FormBuilder, FormGroup, FormsModule} from '@angular/forms';
import {forkJoin} from 'rxjs';
import {EventService} from '../../../services/event.service';
import {EventCard, Category} from '../../../models/event.models';
import {EventUtils} from '../../../utils/event-utils';

const FORMATS = [
  {id: 'ONLINE', name: 'Онлайн'},
  {id: 'OFFLINE', name: 'Офлайн'},
  {id: 'HYBRID', name: 'Гибрид'}
];

@Component({
  selector: 'app-events-page',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule],
  templateUrl: './events-page.component.html',
  styleUrls: ['./events-page.component.scss']
})
export class EventsPageComponent implements OnInit {
  allEvents: EventCard[] = [];
  filteredEvents: EventCard[] = [];
  categories: Category[] = [];
  formats = FORMATS;

  isLoading = true;
  currentPage = 0;
  pageSize = 9;
  totalPages = 0;

  filterForm!: FormGroup;
  searchKeyword = '';

  formatDate = EventUtils.formatDate;
  formatTime = EventUtils.formatTime;
  formatPrice = EventUtils.formatPrice;
  colorFromCode = EventUtils.colorFromCode;

  constructor(
    private eventService: EventService,
    private fb: FormBuilder
  ) {
  }

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  private initForm(): void {
    this.filterForm = this.fb.group({
      categoryId: [null],
      formatId: [null],
      dateFrom: [null],
      dateTo: [null]
    });
  }

  private loadData(): void {
    this.isLoading = true;
    forkJoin({
      categories: this.eventService.getCategories(),
      events: this.eventService.getActiveEvents()
    }).subscribe({
      next: (res) => {
        this.categories = res.categories;
        this.processEvents(res.events);
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  private processEvents(data: EventCard[]): void {
    this.allEvents = data.map(event => ({
      ...event,
      categoryColor: this.categories.find(c => c.idEventCategory === event.idEventCategory)?.colorCode || '#9c89ff'
    })).sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());

    this.applyFiltersAndPagination();
  }

  onSearch(): void {
    this.isLoading = true;
    const request$ = this.searchKeyword.trim()
      ? this.eventService.searchEvents(this.searchKeyword)
      : this.eventService.getActiveEvents();

    request$.subscribe({
      next: (data) => {
        this.processEvents(data);
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  applyFiltersAndPagination(): void {
    const f = this.filterForm.value;
    let filtered = [...this.allEvents];

    if (f.categoryId) filtered = filtered.filter(e => e.idEventCategory === f.categoryId);
    if (f.formatId) filtered = filtered.filter(e => e.eventFormat === f.formatId);

    if (f.dateFrom) {
      const dFrom = new Date(f.dateFrom).setHours(0, 0, 0, 0);
      filtered = filtered.filter(e => new Date(e.eventDate).getTime() >= dFrom);
    }
    if (f.dateTo) {
      const dTo = new Date(f.dateTo).setHours(23, 59, 59, 999);
      filtered = filtered.filter(e => new Date(e.eventDate).getTime() <= dTo);
    }

    this.totalPages = Math.ceil(filtered.length / this.pageSize);
    const start = this.currentPage * this.pageSize;
    this.filteredEvents = filtered.slice(start, start + this.pageSize);
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.applyFiltersAndPagination();
  }

  resetFilters(): void {
    this.filterForm.reset();
    this.searchKeyword = '';
    this.currentPage = 0;
    this.onSearch();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.applyFiltersAndPagination();
      window.scrollTo({top: 0, behavior: 'smooth'});
    }
  }

  get pages(): number[] {
    return Array.from({length: this.totalPages}, (_, i) => i);
  }
}
