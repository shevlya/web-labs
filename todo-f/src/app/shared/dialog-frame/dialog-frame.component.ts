import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-dialog-frame',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dialog-frame.component.html',
  styleUrl: './dialog-frame.component.scss'
})
export class DialogFrameComponent {
  @Input() isVisible = false;
  @Input() title = 'Подтверждение';
  @Input() message = 'Вы уверены?';
  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  onConfirm(): void {
    this.confirmed.emit();
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}
