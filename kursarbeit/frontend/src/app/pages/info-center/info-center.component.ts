import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ContactService} from '../../services/contact.service';
import {AuthService} from '../../services/auth.service';

const FAQ_ITEMS = [
  {
    question: 'Как записаться на мероприятие?',
    answer: 'Найдите интересующее мероприятие в разделе «Мероприятия», откройте его страницу и нажмите кнопку «Записаться». Для записи необходимо быть авторизованным.'
  },
  {
    question: 'Как стать организатором?',
    answer: 'Перейдите в раздел «Профиль» и нажмите кнопку «Подать заявку» в блоке «Стать организатором». Администратор рассмотрит заявку в течение 1-2 рабочих дней.'
  },
  {
    question: 'Что такое лист ожидания?',
    answer: 'Если все места на мероприятии заняты, вы можете встать в лист ожидания. Как только кто-то отменит запись, вы получите место и уведомление.'
  },
  {
    question: 'Как отменить запись?',
    answer: 'В разделе «Мои мероприятия» найдите нужное событие и нажмите «Отменить запись».'
  },
  {
    question: 'Как изменить данные профиля?',
    answer: 'В разделе «Профиль» вы можете сменить аватар, пароль и выбрать любимые категории.'
  },
  {
    question: 'Мероприятие не появляется после создания - почему?',
    answer: 'Новые события проходят модерацию. Обычно проверка занимает до 24 часов.'
  }
];

@Component({
  selector: 'app-info-center',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './info-center.component.html',
  styleUrl: './info-center.component.scss'
})
export class InfoCenterComponent implements OnInit {
  faqItems = FAQ_ITEMS;
  openFaqIndex: number | null = null;
  contactForm!: FormGroup;
  isSending = false;
  sendSuccess = false;
  sendError: string | null = null;
  formSubmitted = false;

  constructor(
    private fb: FormBuilder,
    private contactService: ContactService,
    private authService: AuthService
  ) {
  }

  ngOnInit(): void {
    this.contactForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      subject: ['', [Validators.required, Validators.maxLength(200)]],
      message: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]]
    });

    this.authService.user$.subscribe(user => {
      if (user) {
        this.contactForm.patchValue({name: user.fio, email: user.email});
      }
    });
  }

  toggleFaq(index: number): void {
    this.openFaqIndex = this.openFaqIndex === index ? null : index;
  }

  fieldError(field: string): boolean {
    const c = this.contactForm.get(field);
    return !!c && c.invalid && (c.touched || this.formSubmitted);
  }

  get messageLength(): number {
    return this.contactForm.get('message')?.value?.length ?? 0;
  }

  submitForm(): void {
    this.formSubmitted = true;
    if (this.contactForm.invalid) return;

    this.isSending = true;
    this.contactService.sendMessage(this.contactForm.value).subscribe({
      next: () => {
        this.isSending = false;
        this.sendSuccess = true;
        this.contactForm.reset();
        this.formSubmitted = false;
      },
      error: (err) => {
        this.isSending = false;
        this.sendError = err.error?.message ?? 'Ошибка отправки. Попробуйте позже.';
      }
    });
  }

  resetSuccess(): void {
    this.sendSuccess = false;
  }
}
