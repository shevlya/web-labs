export class EventUtils {
  static formatDate(dateStr: string | null | undefined): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('ru-RU', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    });
  }

  static formatTime(timeStr: string | null | undefined): string {
    if (!timeStr) return '';
    return new Date(timeStr).toLocaleTimeString('ru-RU', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  static formatPrice(price: number): string {
    return price === 0 ? 'Бесплатно' : `${price} ₽`;
  }

  static colorFromCode(code: string | null | undefined): string {
    if (!code) return 'rgba(101, 87, 115, 0.85)';
    if (code.startsWith('#')) {
      const r = parseInt(code.slice(1, 3), 16);
      const g = parseInt(code.slice(3, 5), 16);
      const b = parseInt(code.slice(5, 7), 16);
      return `rgba(${r},${g},${b},0.85)`;
    }
    return code;
  }
}
