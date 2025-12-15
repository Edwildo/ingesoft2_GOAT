import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { orderService, Order } from "../../api/order.service";
import { Card } from "../../components/common/Card";
import { Button } from "../../components/common/Button";
import { Loading } from "../../components/common/Loading";
import { Alert } from "../../components/common/Alert";
import styles from "./MyOrdersPage.module.css";

export const MyOrdersPage: React.FC = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await orderService.getMyOrders();
      if (response.success && response.data) {
        setOrders(response.data);
      } else {
        setError(response.error?.message || "Error al cargar las órdenes");
      }
    } catch (err) {
      console.error(err);
      setError("Error de conexión con el servidor");
    } finally {
      setIsLoading(false);
    }
  };

  const formatPrice = (price: number): string => {
    return new Intl.NumberFormat("es-CO", {
      style: "currency",
      currency: "COP",
      minimumFractionDigits: 0,
    }).format(price);
  };

  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString("es-CO", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
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

  const getStatusClass = (status: string): string => {
    const classes: Record<string, string> = {
      PENDING: styles.statusPending,
      PAYMENT_PENDING: styles.statusPaymentPending,
      CONFIRMED: styles.statusConfirmed,
      SHIPPING: styles.statusShipping,
      DELIVERED: styles.statusDelivered,
      CANCELLED: styles.statusCancelled,
    };
    return classes[status] || "";
  };

  if (isLoading) {
    return (
      <div className={styles.ordersPage}>
        <div className={styles.container}>
          <Loading message="Cargando órdenes..." />
        </div>
      </div>
    );
  }

  return (
    <div className={styles.ordersPage}>
      <div className={styles.container}>
        <div className={styles.header}>
          <h1 className={styles.title}>Mis órdenes</h1>
        </div>

        {error && (
          <Alert variant="error" onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {orders.length === 0 ? (
          <div className={styles.empty}>
            <p>No tienes órdenes aún.</p>
            <Button variant="primary" onClick={() => navigate("/shop")}>
              Ir al shop
            </Button>
          </div>
        ) : (
          <div className={styles.ordersList}>
            {orders.map((order) => (
              <Card key={order.id} className={styles.orderCard}>
                <div className={styles.orderHeader}>
                  <div>
                    <h3>Orden #{order.id.substring(0, 8).toUpperCase()}</h3>
                    <p className={styles.orderDate}>
                      {formatDate(order.createdAt)}
                    </p>
                  </div>
                  <span className={`${styles.status} ${getStatusClass(order.status)}`}>
                    {getStatusLabel(order.status)}
                  </span>
                </div>

                <div className={styles.orderItems}>
                  {order.items.map((item) => (
                    <div key={item.id} className={styles.orderItem}>
                      {item.coverImage && (
                        <img
                          src={item.coverImage}
                          alt={item.brand}
                          className={styles.itemImage}
                          onError={(e) => {
                            (e.target as HTMLImageElement).src =
                              "/placeholder-sneaker.png";
                          }}
                        />
                      )}
                      <div className={styles.itemInfo}>
                        <strong>{item.brand}</strong>
                        <span>Talla: {item.size}</span>
                        <span>Color: {item.color}</span>
                      </div>
                      <div className={styles.itemPrice}>
                        {formatPrice(item.price)}
                      </div>
                    </div>
                  ))}
                </div>

                <div className={styles.orderFooter}>
                  <div className={styles.orderTotal}>
                    <span>Total:</span>
                    <strong>{formatPrice(order.totalAmount)}</strong>
                  </div>
                  <Button
                    variant="outline"
                    onClick={() => navigate(`/orders/${order.id}`)}
                  >
                    Ver detalles
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
