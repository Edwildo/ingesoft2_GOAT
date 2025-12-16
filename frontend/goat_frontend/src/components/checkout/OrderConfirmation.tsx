import React from "react";
import { Order } from "../../api/order.service";
import { Button } from "../common/Button";
import styles from "./CheckoutModal.module.css";

interface OrderConfirmationProps {
  order: Order;
  onClose: () => void;
}

export const OrderConfirmation: React.FC<OrderConfirmationProps> = ({
  order,
  onClose,
}) => {

  const formatPrice = (price: number): string => {
    return new Intl.NumberFormat("es-CO", {
      style: "currency",
      currency: "COP",
      minimumFractionDigits: 0,
    }).format(price);
  };

  const getStatusLabel = (status: string): string => {
    const labels: Record<string, string> = {
      PENDING: "Pendiente",
      PAYMENT_PENDING: "Pago en proceso",
      CONFIRMED: "Confirmada",
      SHIPPING: "En envío",
      DELIVERED: "Entregada",
      CANCELLED: "Cancelada",
    };
    return labels[status] || status;
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.confirmation}>
          <div className={styles.successIcon}>✓</div>
          <h2>¡Orden confirmada!</h2>
          <p className={styles.orderNumber}>
            Número de orden: <strong>{order.id.substring(0, 8).toUpperCase()}</strong>
          </p>
          <p className={styles.status}>
            Estado: <strong>{getStatusLabel(order.status)}</strong>
          </p>

          <div className={styles.orderDetails}>
            <div className={styles.detailRow}>
              <span>Total:</span>
              <strong>{formatPrice(order.totalAmount)}</strong>
            </div>
            <div className={styles.detailRow}>
              <span>Método de envío:</span>
              <span>
                {order.shippingMethod === "STANDARD" ? "Estándar" : "Express"}
              </span>
            </div>
            <div className={styles.detailRow}>
              <span>Dirección:</span>
              <span>
                {order.shippingAddress.street}, {order.shippingAddress.city},{" "}
                {order.shippingAddress.state}, {order.shippingAddress.postalCode},{" "}
                {order.shippingAddress.country}
              </span>
            </div>
            {order.paymentId && (
              <div className={styles.detailRow}>
                <span>Payment ID:</span>
                <span>{order.paymentId}</span>
              </div>
            )}
          </div>

          <div className={styles.actions}>
            <Button variant="primary" onClick={onClose}>
              Cerrar
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

